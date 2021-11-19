package io.grpc.examples.helloworld

import java.util.logging.Logger

import io.grpc.{Server, ServerBuilder}
import io.grpc.examples.helloworld.helloworld.{GreeterGrpc, HelloRequest, HelloReply}

import scala.concurrent.{ExecutionContext, Future}

/**
 * [[https://github.com/grpc/grpc-java/blob/v0.15.0/examples/src/main/java/io/grpc/examples/helloworld/HelloWorldServer.java]]
 */
object HelloWorldServer {
  private val logger = Logger.getLogger(classOf[HelloWorldServer].getName)
  private val port = 50051

  def main(args: Array[String]): Unit = {
    val server = new HelloWorldServer(ExecutionContext.global, port)
    server.start()
    server.blockUntilShutdown()
  }
}

class HelloWorldServer(executionContext: ExecutionContext, port: Int) { self =>
  var server: Server = null

  def start(): Unit = {
    server = ServerBuilder.forPort(port)
        .addService(GreeterGrpc.bindService(new GreeterImpl, executionContext))
        .build
        .start
    HelloWorldServer.logger.info("Server started, listening on " + port)
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

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  class GreeterImpl extends GreeterGrpc.Greeter {
    override def sayHello(req: HelloRequest) = {
      val reply = HelloReply(message = "Hello " + req.name)
      Future.successful(reply)
    }

    override def sayHelloAgain(req: HelloRequest) = {
      val reply = HelloReply(message = "Hello Again " + req.name)
      Future.successful(reply)
    }
  }

}

