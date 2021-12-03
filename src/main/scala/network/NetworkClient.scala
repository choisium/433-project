/*
  Make network request for connection to master.
  NetworkClient send request, and NetworkClient responed to the request.
  main.Worker set this client.
*/

package network

import scala.io.Source
import scala.collection.mutable.Map
import scala.concurrent.{Promise}
import scala.annotation.tailrec

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

  var shuffleHandler: ShuffleHandler = null;

  var id: Int = -1
  lazy val baseDirPath = {
    assert (id > 0)
    System.getProperty("user.dir") + "/src/main/resources/" + id
  }
  var workerNum: Int = -1
  val workers = Map[Int, WorkerInfo]()

  final def shutdown: Unit = {
    if (id > 0) {
      val response = blockingStub.terminate(new TerminateRequest(id))
    }
    if (shuffleHandler != null) {
      shuffleHandler.serverStop
    }
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  final def requestConnect(host: String, port: Int): Unit = {
    logger.info("[requestConnect] try to connect to master")
    val response = blockingStub.connect(new ConnectRequest(host, port))
    id = response.id
    shuffleHandler = new ShuffleHandler(host, port, id)
  }

  final def sample(): Unit = {
    logger.info("[sample] start Sample")
    /* TODO: Need to get input directory from user command */
    val inputDirPath = baseDirPath + "/input"

    Sampler.sample(inputDirPath, baseDirPath, 100)
    assert (new File(baseDirPath + "/sample").isFile)
  }

  final def requestSample(samplePromise: Promise[Unit]): Unit = {
    logger.info("[requestSample] Try to send sample")

    val responseObserver = new StreamObserver[SampleResponse]() {
      override def onNext(response: SampleResponse): Unit = {
        if (response.status == StatusEnum.SUCCESS) {
          samplePromise.success()
        }
      }

      override def onError(t: Throwable): Unit = {
        logger.warning(s"[requestSample] Server response Failed: ${Status.fromThrowable(t)}")
        samplePromise.failure(new WorkerFailedException)
      }

      override def onCompleted(): Unit = {
        logger.info("[requestSample] Done sending sample")
      }
    }

    val requestObserver = asyncStub.sample(responseObserver)

    try {
      val samplePath = baseDirPath + "/sample"
      val source = Source.fromFile(samplePath)
      for (line <- source.getLines) {
        val request = SampleRequest(id = id, data = ByteString.copyFromUtf8(line+"\n"))
        requestObserver.onNext(request)
      }
      source.close
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

  @tailrec
  final def requestPivot(): Unit = {
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
        logger.info("[requestPivot] Pivot failed.")
        throw new WorkerFailedException
      }
      case _ => {
        /* Wait 5 seconds and retry */
        Thread.sleep(5 * 1000)
        requestPivot
      }
    }
  }

  final def sort(): Unit = {
    logger.info("[sort] start Sort")
    // Do sort

    // Start shuffle server
    logger.info("[sort] start ShuffleHandler Server")
    shuffleHandler.serverStart
  }

  @tailrec
  final def requestSort(): Unit = {
    logger.info("[requestSort] Notify sort done and shuffling ready")

    val response = blockingStub.sort(new SortRequest(id))
    response.status match {
      case StatusEnum.SUCCESS => {
        logger.info("[requestSort] Other workers are shuffling ready too")
      }
      case StatusEnum.FAILED => {
        logger.info("[requestSort] RequestSort failed.")
        throw new WorkerFailedException
      }
      case _ => {
        /* Wait 5 seconds and retry */
        Thread.sleep(5 * 1000)
        requestSort
      }
    }
  }

  final def shuffle(): Unit = {
    logger.info("[shuffle] start Shuffle")
    shuffleHandler.shuffle(workers)
  }
}
