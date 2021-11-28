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

import message.connection.ConnectionGrpc
import message.connection._
import common.WorkerInfo


class NetworkClient(host: String, port: Int) {
  val logger: Logger = Logger.getLogger(classOf[NetworkClient].getName)

  val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext.build
  val blockingStub = ConnectionGrpc.blockingStub(channel)
  val asyncStub = ConnectionGrpc.stub(channel)

  var id: Int = -1
  val baseDirPath = System.getProperty("user.dir") + "/src/main/resources/"
  var workerNum: Int = -1
  val workers = Map[Int, WorkerInfo]()

  def shutdown: Unit = {
    if (id > 0) {
      val response = blockingStub.terminate(new TerminateRequest(id))
    } else {}
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  def connect(ip: String, port: Int): Boolean = {
    val response = blockingStub.connect(new ConnectRequest(ip, port))
    id = response.id
    logger.info("Connection result: " + response.success)
    response.success
  }

  def pivot(pivotPromise: Promise[Unit]): Unit = {
    logger.info("*** DataRoute")
    val samplePath = s"$baseDirPath/$id/sample"
    assert (new File(samplePath).isFile)

    val responseObserver = new StreamObserver[PivotResponse]() {
      override def onNext(response: PivotResponse): Unit = {
        workerNum = response.workerNum
        for (w <- response.workers) {
          workers(w.id) = WorkerInfo.convertMessageToInfo(w)
        }

        for ((id, w) <- workers) {
          println(id, w.keyRange)
        }
        logger.info("DataRoute - Server response onNext")
        pivotPromise.success()
      }

      override def onError(t: Throwable): Unit = {
        logger.warning(s"DataRoute - Server response Failed: ${Status.fromThrowable(t)}")
      }

      override def onCompleted(): Unit = {
        logger.info("DataRoute - Server response onCompleted")
      }
    }

    val requestObserver = asyncStub.pivot(responseObserver)

    try {
      for (line <- Source.fromFile(samplePath).getLines) {
        val request = PivotRequest(id = id, data = ByteString.copyFromUtf8(line+"\n"))
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
}
