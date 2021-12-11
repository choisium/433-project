/*
  Respond to network request for file shuffle.
  FileClient send file, and FileServer get file.
  main.Worker set this server.
*/

package network

import java.util.logging.Logger
import java.io.{OutputStream, FileOutputStream}
import java.util.concurrent.TimeUnit

import io.grpc.{Server, ServerBuilder, Status}
import io.grpc.stub.StreamObserver;

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

import message.common.StatusEnum
import message.shuffle.{ShuffleGrpc, FileRequest, FileResponse}
import common._


class FileServer(executionContext: ExecutionContext, port: Int, id: Int, tempDir: String) { self =>
  val logger: Logger = Logger.getLogger(classOf[FileServer].getName)
  var server: Server = null

  def start(): Unit = {
    server = ServerBuilder.forPort(port)
        .addService(ShuffleGrpc.bindService(new ShuffleImpl, executionContext))
        .build
        .start
    logger.info("[FileServer] started, listening on " + port)
    sys.addShutdownHook {
      logger.info("Shutting down FileServer since JVM is shutting down")
      self.stop()
      logger.info("File server shut down")
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  class ShuffleImpl() extends ShuffleGrpc.Shuffle {
    override def shuffle(responseObserver: StreamObserver[FileResponse]): StreamObserver[FileRequest] = {
      new StreamObserver[FileRequest] {
        var writer: FileOutputStream = null
        var senderId: Int = -1
        var partitionId: Int = -1

        override def onNext(request: FileRequest): Unit = {
          senderId = request.id
          partitionId = request.partitionId
          if (writer == null) {
            logger.info(s"[FileServer]: getting from $senderId with partition $partitionId")
            val file = FileHandler.createFile(tempDir, s"shuffle-$senderId-$partitionId-", "-unsorted")
            writer = new FileOutputStream(file)
          }
          request.data.writeTo(writer)
          writer.flush
        }

        override def onError(t: Throwable): Unit = {
          logger.warning(s"[FileServer]: Worker $senderId failed to send partition $partitionId: ${Status.fromThrowable(t)}")
          throw t
        }

        override def onCompleted(): Unit = {
          logger.info(s"[FileServer]: Worker $senderId done sending partition $partitionId")

          writer.close
          responseObserver.onNext(new FileResponse(StatusEnum.SUCCESS))
          responseObserver.onCompleted
        }
      }
    }
  }
}
