/*
  Respond to network request for connection to master.
  NetworkClient send request, and NetworkClient responed to the request.
  main.Master set this client.
*/

package network

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.Map

import java.util.logging.Logger
import java.io.{OutputStream, BufferedOutputStream, FileOutputStream}

import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver;

import message.connection.ConnectionGrpc
import message.connection._
import common.{WorkerInfo, InvalidStateException}


// State case objects
sealed trait MasterState
case object MASTERINIT extends MasterState
case object CONNECTED extends MasterState
case object SORTED extends MasterState
case object TERMINATE extends MasterState


class NetworkServer(executionContext: ExecutionContext, port: Int, requiredWorkerNum: Int) { self =>
  require(requiredWorkerNum > 0, "requiredWorkerNum should be positive")

  val logger = Logger.getLogger(classOf[NetworkServer].getName)
  var server: Server = null
  val workers = Map[Int, WorkerInfo]()
  var state: MasterState = MASTERINIT

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
      server.shutdown
    }
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination
    }
  }

  class ConnectionImpl() extends ConnectionGrpc.Connection {
    override def connect(request: ConnectRequest): Future[ConnectResponse] = {
      if (state != MASTERINIT) {
        Future.failed(new InvalidStateException)
      }

      workers.synchronized {
        if (workers.size < requiredWorkerNum) {
          workers(workers.size + 1) = new WorkerInfo(request.ip, request.port);
          if (workers.size == requiredWorkerNum) {
            state = CONNECTED
          }
          Future.successful(new ConnectResponse(true, workers.size))
        } else {
          Future.successful(new ConnectResponse(false, -1))
        }
      }
    }

    override def terminate(request: TerminateRequest): Future[TerminateResponse] = {
      workers.synchronized {
        val worker = workers.remove(request.id)
        if (state != MASTERINIT && workers.size == 0) {
          state = TERMINATE
          stop
        }
        Future.successful(new TerminateResponse)
      }
    }
  }
}
