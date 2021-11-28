/*
  Respond to network request for connection to master.
  NetworkClient send request, and NetworkClient responed to the request.
  main.Master set this client.
*/

package network

import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.Map
import scala.util.{Success, Failure}

import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import java.io.{OutputStream, BufferedOutputStream, FileOutputStream, File}

import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver;

import message.connection.ConnectionGrpc
import message.connection._
import common.{WorkerInfo, WORKERINIT, SAMPLED, PARTITIONED, SHUFFLED, DONE}
import sorting.Pivoting


// State case objects
sealed trait MasterState
case object MASTERINIT extends MasterState
case object CONNECTED extends MasterState
case object PIVOTED extends MasterState
case object SORTED extends MasterState
case object TERMINATE extends MasterState


class NetworkServer(executionContext: ExecutionContext, port: Int, requiredWorkerNum: Int) { self =>
  require(requiredWorkerNum > 0, "requiredWorkerNum should be positive")

  val logger = Logger.getLogger(classOf[NetworkServer].getName)

  var server: Server = null
  val workers = Map[Int, WorkerInfo]()
  var state: MasterState = MASTERINIT

  val baseDirPath = System.getProperty("user.dir") + "/src/main/resources/master"

  val pivotPromise = Promise[PivotResponse]()

  def createBaseDir(): Unit = {
    val baseDir = new File(baseDirPath)
    if (!baseDir.exists) {
      baseDir.mkdir  // need to handle exception
    }
    assert(baseDir.exists, "after create base directory")
  }

  def deleteFilesInBaseDir(): Unit = {
    val baseDir = new File(baseDirPath)
    for (file <- baseDir.listFiles) {
      file.delete
    }
    assert(baseDir.exists && baseDir.listFiles.length == 0)
  }

  def start(): Unit = {
    createBaseDir
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
      server.shutdown.awaitTermination(5, TimeUnit.SECONDS)
    }
    deleteFilesInBaseDir
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination
    }
  }

  def workersToMessage(): Seq[WorkerMessage] = {
    (workers.map{case (id, worker) => WorkerInfo.convertToWorkerMessage(worker)}).toSeq
  }

  class ConnectionImpl() extends ConnectionGrpc.Connection {
    override def connect(request: ConnectRequest): Future[ConnectResponse] = {
      if (state != MASTERINIT) {
        Future.successful(new ConnectResponse(false, -1))
      }

      workers.synchronized {
        if (workers.size < requiredWorkerNum) {
          workers(workers.size + 1) = new WorkerInfo(workers.size + 1, request.ip, request.port);
          if (workers.size == requiredWorkerNum) {
            state = CONNECTED
          }
          Future.successful(new ConnectResponse(true, workers.size))
        } else {
          Future.successful(new ConnectResponse(false, -1))
        }
      }
    }

    override def pivot(responseObserver: StreamObserver[PivotResponse]): StreamObserver[PivotRequest] = {
      assert (state == MASTERINIT || state == CONNECTED)
      new StreamObserver[PivotRequest] {
        val filepath = baseDirPath + "/sample"
        var writer: BufferedOutputStream = null
        var workerId: Int = -1

        override def onNext(request: PivotRequest): Unit = {
          workerId = request.id
          if (writer == null) {
            writer = new BufferedOutputStream(new FileOutputStream(filepath, true))
          }
          request.data.writeTo(writer)
          writer.flush
        }

        override def onError(t: Throwable): Unit = {
          logger.warning("Sample cancelled")
        }

        override def onCompleted(): Unit = {
          writer.close
          workers.synchronized{
            workers(workerId).state = SAMPLED
          }

          if (state == CONNECTED && workers.size == requiredWorkerNum && workers.forall {case (_, worker) => worker.state == SAMPLED}) {
            val pivot = new Pivoting(filepath, requiredWorkerNum, 1);
            val keyRange = pivot.run
            for ((id, worker) <- workers) {
              worker.keyRange = keyRange(id - 1)
              println(id, worker.keyRange)
            }

            pivotPromise.success(PivotResponse(
              workerNum = requiredWorkerNum,
              workers = workersToMessage
            ))
            state = PIVOTED
          }

          pivotPromise.future.onComplete {
            case Success(pivotResponse) => {
              println("success sending pivot message")
              responseObserver.onNext(pivotResponse)
              responseObserver.onCompleted
            }
          }
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
