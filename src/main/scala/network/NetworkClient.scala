/*
  Make network request for connection to master.
  NetworkClient send request, and NetworkClient responed to the request.
  main.Worker set this client.
*/

package network

import scala.io.Source
import scala.collection.mutable.Map
import scala.concurrent.{Promise}

import com.google.protobuf.ByteString

import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import java.io.File

import io.grpc.{ManagedChannelBuilder, Status}
import io.grpc.stub.StreamObserver

import message.common._
import message.connection._
import common._
import sorting.Sampler


class NetworkClient(host: String, port: Int) {
  val logger: Logger = Logger.getLogger(classOf[NetworkClient].getName)

  val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext.build
  val blockingStub = ConnectionGrpc.blockingStub(channel)
  val asyncStub = ConnectionGrpc.stub(channel)

  var id: Int = -1
  lazy val baseDirPath = {
    assert (id > 0)
    System.getProperty("user.dir") + "/src/main/resources/" + id
  }
  var workerNum: Int = -1
  val workers = Map[Int, WorkerInfo]()

  def shutdown: Unit = {
    if (id > 0) {
      val response = blockingStub.terminate(new TerminateRequest(id))
    }
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  def requestConnect(ip: String, port: Int): Unit = {
    val response = blockingStub.connect(new ConnectRequest(ip, port))
    id = response.id
  }

  def sample(): Unit = {
    logger.info("[sample] start Sample")
    /* TODO: Need to get input directory from user command */
    val inputDirPath = baseDirPath + "/input"

    Sampler.sample(inputDirPath, baseDirPath, 100)
    assert (new File(baseDirPath + "/sample").isFile)
  }

  def requestSample(samplePromise: Promise[Unit]): Unit = {
    logger.info("[requestSample] Try to send sample")

    val responseObserver = new StreamObserver[SampleResponse]() {
      override def onNext(response: SampleResponse): Unit = {
        if (response.status == StatusEnum.SUCCESS) {
          samplePromise.success()
        }
      }

      override def onError(t: Throwable): Unit = {
        logger.warning(s"[requestSample] Server response Failed: ${Status.fromThrowable(t)}")
      }

      override def onCompleted(): Unit = {
        logger.info("[requestSample] Done sending sample")
      }
    }

    val requestObserver = asyncStub.sample(responseObserver)

    try {
      val samplePath = baseDirPath + "/sample"
      for (line <- Source.fromFile(samplePath).getLines) {
        val request = SampleRequest(id = id, data = ByteString.copyFromUtf8(line+"\n"))
        requestObserver.onNext(request)
      }
    } catch {
      case e: RuntimeException => {
        // Cancel RPC
        requestObserver.onError(e)
        throw e
      }
    }

    // Mark the end of requests
    requestObserver.onCompleted()
  }

  def requestPivot(): Unit = {
    logger.info("[requestPivot] Try to get pivot")

    val response = blockingStub.pivot(new PivotRequest(id))
    response.status match {
      case StatusEnum.SUCCESS => {
        workerNum = response.workerNum
        for (w <- response.workers) {
          workers(w.id) = WorkerInfo.convertMessageToInfo(w)
        }
        assert (workers.size == workerNum)
      }
      case StatusEnum.FAILED => {
        logger.info("[Pivot] Pivot failed.")
        throw new PivotingFailedException
      }
      case _ => {
        /* Wait 5 seconds and retry */
        Thread.sleep(5 * 1000)
        requestPivot
      }
    }
  }
}
