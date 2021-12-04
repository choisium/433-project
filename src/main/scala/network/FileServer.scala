/*
  Respond to network request for file shuffle.
  FileClient send file, and FileServer get file.
  main.Worker set this server.
*/

package network

import java.util.logging.Logger
import java.io.{OutputStream, BufferedOutputStream, FileOutputStream}

import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver;

import scala.concurrent.ExecutionContext

import message.common.StatusEnum
import message.shuffle.{ShuffleGrpc, FileRequest, FileResponse}


class ShuffleImpl() extends ShuffleGrpc.Shuffle {
  val logger: Logger = Logger.getLogger(classOf[ShuffleImpl].getName)

  override def dataRoute(responseObserver: StreamObserver[FileResponse]): StreamObserver[FileRequest] =
    new StreamObserver[FileRequest] {
      val filepath = "/Users/choisium/Development/433-project/src/main/resources/result.txt"
      val writer = new BufferedOutputStream(new FileOutputStream(filepath))


      override def onNext(request: FileRequest): Unit = {
        request.data.writeTo(writer)
        writer.flush
      }

      override def onError(t: Throwable): Unit = {
        logger.warning("DataRoute cancelled")
      }

      override def onCompleted(): Unit = {
        writer.close
        responseObserver.onNext(FileResponse(StatusEnum.SUCCESS, "Done"))
        responseObserver.onCompleted
      }
    }
}

class FileServer(executionContext: ExecutionContext, port: Int) { self =>
  val logger: Logger = Logger.getLogger(classOf[FileServer].getName)
  var server: Server = null

  def start(): Unit = {
    server = ServerBuilder.forPort(port)
        .addService(ShuffleGrpc.bindService(new ShuffleImpl, executionContext))
        .build
        .start
    logger.info("Server started, listening on " + port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }
}
