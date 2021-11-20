package master

import scala.concurrent.{ExecutionContext, Future}

import java.util.logging.Logger
import java.io.{OutputStream, BufferedOutputStream, FileOutputStream}

import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver;

import message.connection.{ConnectionGrpc, ConnectRequest, ConnectResponse}


class NetworkServer(executionContext: ExecutionContext, port: Int, requiredWorkerNum: Int) { self =>
  val logger: Logger = Logger.getLogger(classOf[NetworkServer].getName)
  var server: Server = null

  def start(): Unit = {
    server = ServerBuilder.forPort(port)
      .addService(ConnectionGrpc.bindService(new ConnectionImpl, executionContext))
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

  class ConnectionImpl() extends ConnectionGrpc.Connection {
    override def connect(request: ConnectRequest): Future[ConnectResponse] = {
      Future.successful(new ConnectResponse(1, true))
    }
  }
}
