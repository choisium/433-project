/*
  Make network request for file shuffle.
  FileClient send file, and FileServer get file.
  main.Worker set this client.
*/

package network

import com.google.protobuf.ByteString

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import scala.io.Source
import scala.concurrent.Promise
import scala.concurrent.duration._

import io.grpc.{ManagedChannelBuilder, Status}
import io.grpc.stub.StreamObserver

import message.common.StatusEnum
import message.shuffle.{ShuffleGrpc, FileRequest, FileResponse}
import common._

class FileClient(host: String, port: Int, id: Int) {
  val logger: Logger = Logger.getLogger(classOf[FileClient].getName)

  val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext.build
  val blockingStub = ShuffleGrpc.blockingStub(channel)
  val asyncStub = ShuffleGrpc.stub(channel)

  def shutdown(): Unit = {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
  }

  def requestShuffle(filepath: String, shufflePromise: Promise[Unit]): Unit = {
    logger.info("[FileClient] Try to send partition")

    val responseObserver = new StreamObserver[FileResponse]() {
      override def onNext(response: FileResponse): Unit = {
        if (response.status == StatusEnum.SUCCESS) {
          shufflePromise.success()
        }
      }

      override def onError(t: Throwable): Unit = {
        logger.warning(s"[FileClient] Server response Failed: ${Status.fromThrowable(t)}")
        shufflePromise.failure(new WorkerFailedException)
      }

      override def onCompleted(): Unit = {
        logger.info("[FileClient] Done sending partition")
      }
    }

    val requestObserver = asyncStub.shuffle(responseObserver)

    try {
      val source = Source.fromFile(filepath)
      for (line <- source.getLines) {
        val request = FileRequest(id = id, data = ByteString.copyFromUtf8(line+"\n"))
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
}
