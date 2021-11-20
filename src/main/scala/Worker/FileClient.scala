package worker


import com.google.protobuf.ByteString

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import io.grpc.{ManagedChannelBuilder, Status}
import io.grpc.stub.StreamObserver

import scala.io.Source

import message.shuffle.{ShuffleGrpc, FileRequest, FileResponse}


class FileClient(host: String, port: Int) {
  val logger: Logger = Logger.getLogger(classOf[FileClient].getName)

  val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext.build
  val blockingStub = ShuffleGrpc.blockingStub(channel)
  val asyncStub = ShuffleGrpc.stub(channel)

  def shutdown(): Unit = {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
  }

  def dataRoute(filepath: String): Unit = {
    logger.info("*** DataRoute")

    val responseObserver = new StreamObserver[FileResponse]() {
      override def onNext(response: FileResponse): Unit = {
        logger.info("DataRoute - Server response onNext")
      }

      override def onError(t: Throwable): Unit = {
        logger.warning(s"DataRoute - Server response Failed: ${Status.fromThrowable(t)}")
      }

      override def onCompleted(): Unit = {
        logger.info("DataRoute - Server response onCompleted")
      }
    }

    val requestObserver = asyncStub.dataRoute(responseObserver)

    try {
      for (line <- Source.fromFile(filepath).getLines) {
        logger.info(s"Sending message '${line}'")
        val request = FileRequest(data = ByteString.copyFromUtf8(line+"\n"))
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
