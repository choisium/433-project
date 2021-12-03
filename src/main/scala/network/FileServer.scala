/*
  Respond to network request for file shuffle.
  FileClient send file, and FileServer get file.
  main.Worker set this server.
*/

package network

import java.util.logging.Logger
import java.io.{OutputStream, BufferedOutputStream, FileOutputStream}
import java.util.concurrent.TimeUnit

import io.grpc.{Server, ServerBuilder, Status}
import io.grpc.stub.StreamObserver;

import scala.concurrent.ExecutionContext

import message.common.StatusEnum
import message.shuffle.{ShuffleGrpc, FileRequest, FileResponse}


class FileServer(executionContext: ExecutionContext, port: Int, id: Int) { self =>
  val logger: Logger = Logger.getLogger(classOf[FileServer].getName)
  val baseDir = s"${System.getProperty("user.dir")}/src/main/resources/${id}"
  var server: Server = null

  def start(): Unit = {
    server = ServerBuilder.forPort(port)
        .addService(ShuffleGrpc.bindService(new ShuffleImpl, executionContext))
        .build
        .start
    logger.info("FileServer started, listening on " + port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
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
    override def shuffle(responseObserver: StreamObserver[FileResponse]): StreamObserver[FileRequest] =
      new StreamObserver[FileRequest] {
        val filepath = baseDir + "/shuffle-"
        var writer: BufferedOutputStream = null
        var senderId: Int = -1
        var partitionId: Int = -1

        override def onNext(request: FileRequest): Unit = {
          senderId = request.id
          partitionId = request.partitionId
          if (writer == null) {
            logger.info(s"[FileServer]: getting from $senderId with partition $partitionId")
            writer = new BufferedOutputStream(new FileOutputStream(filepath + senderId + "-" + partitionId))
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
