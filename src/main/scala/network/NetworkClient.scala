/*
  Make network request for connection to master.
  NetworkClient send request, and NetworkClient responed to the request.
  main.Worker set this client.
*/

package network

import scala.io.Source
import scala.collection.mutable.Map
import scala.concurrent.{Promise}
import scala.annotation.tailrec

import com.google.protobuf.ByteString

import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import java.io.File

import io.grpc.{ManagedChannelBuilder, Status}
import io.grpc.stub.StreamObserver

import message.common._
import message.connection._
import common._
import sorting.{Sampler, Sorter, Merger}


class ClientInfo(
  val masterHost: String,
  val masterPort: Int,
  val inputDirs: Seq[String],
  val outputDir: String,
  val fileServerHost: String,
  val fileServerPort: Int
) {}


class NetworkClient(clientInfo: ClientInfo) {
  val logger: Logger = Logger.getLogger(classOf[NetworkClient].getName)
  val channel = ManagedChannelBuilder
                  .forAddress(clientInfo.masterHost, clientInfo.masterPort)
                  .usePlaintext
                  .build
  val blockingStub = ConnectionGrpc.blockingStub(channel)
  val asyncStub = ConnectionGrpc.stub(channel)

  var shuffleHandler: ShuffleHandler = null;
  var tempDir: String = null;

  var id: Int = -1
  var workerNum: Int = -1
  val workers = Map[Int, WorkerInfo]()

  final def shutdown(success: Boolean): Unit = {
    logger.info("[NetworkClient] Client shutdown")
    if (shuffleHandler != null) {
      shuffleHandler.serverStop
    }
    if (tempDir != null) {
      FileHandler.deleteDir(tempDir)
    }

    if (id > 0) {
      val message = new TerminateRequest(id, if (success) StatusEnum.SUCCESS else StatusEnum.FAILED)
      val response = blockingStub.terminate(message)
      id = -1
      channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
    }

    /* Clear temporary files */
  }

  final def requestConnect(): Unit = {
    logger.info("[requestConnect] try to connect to master")
    val response = blockingStub.connect(new ConnectRequest(clientInfo.fileServerHost, clientInfo.fileServerPort))
    id = response.id
    tempDir = FileHandler.createDir(s"blue-worker${id}")
    shuffleHandler = new ShuffleHandler(clientInfo.fileServerHost, clientInfo.fileServerPort, id, tempDir)
  }

  final def sample(): Unit = {
    logger.info("[sample] start Sample")
    val inputDir = clientInfo.inputDirs(0)
    Sampler.sample(inputDir, tempDir, 100)
    logger.info("[sample] done Sample")
  }

  final def requestSample(samplePromise: Promise[Unit]): Unit = {
    logger.info("[requestSample] Try to send sample")

    val responseObserver = new StreamObserver[SampleResponse]() {
      override def onNext(response: SampleResponse): Unit = {
        if (response.status == StatusEnum.SUCCESS) {
          samplePromise.success()
        }
      }

      override def onError(t: Throwable): Unit = {
        logger.warning(s"[requestSample] Server response Failed: ${Status.fromThrowable(t)}")
        samplePromise.failure(new WorkerFailedException)
      }

      override def onCompleted(): Unit = {
        logger.info("[requestSample] Done sending sample")
      }
    }

    val requestObserver = asyncStub.sample(responseObserver)

    try {
      val sampleFiles = FileHandler.getListFilesWithPrefix(tempDir, "sample", "")
      assert(sampleFiles.length == 1)
      val source = Source.fromFile(sampleFiles(0))
      for (line <- source.getLines) {
        val request = SampleRequest(id = id, data = ByteString.copyFromUtf8(line+"\r\n"))
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

  @tailrec
  final def requestPivot(): Unit = {
    logger.info("[requestPivot] Try to get pivot")

    val response = blockingStub.pivot(new PivotRequest(id))
    response.status match {
      case StatusEnum.SUCCESS => {
        workerNum = response.workerNum
        for (w <- response.workers) {
          workers(w.id) = WorkerInfo.convertMessageToInfo(w)
        }
        assert (workers.size == workerNum)
      }
      case StatusEnum.FAILED => {
        logger.info("[requestPivot] Pivot failed.")
        throw new WorkerFailedException
      }
      case _ => {
        /* Wait 5 seconds and retry */
        Thread.sleep(5 * 1000)
        requestPivot
      }
    }
  }

  final def sort(): Unit = {
    // Do sort
    logger.info("[sort] start Sort")
    val mainRange = for ((workerId, worker) <- workers) yield workerId -> worker.keyRange
    Sorter.partition(clientInfo.inputDirs, tempDir, mainRange.toMap)
    logger.info("[sort] done Sort")

    // Start shuffle server
    logger.info("[sort] start ShuffleHandler Server")
    shuffleHandler.serverStart
  }

  @tailrec
  final def requestSort(): Unit = {
    logger.info("[requestSort] Notify sort done and shuffling ready")

    val response = blockingStub.sort(new SortRequest(id))
    response.status match {
      case StatusEnum.SUCCESS => {
        logger.info("[requestSort] Other workers are shuffling ready too")
      }
      case StatusEnum.FAILED => {
        logger.info("[requestSort] RequestSort failed.")
        throw new WorkerFailedException
      }
      case _ => {
        /* Wait 5 seconds and retry */
        Thread.sleep(5 * 1000)
        requestSort
      }
    }
  }

  final def shuffle(): Unit = {
    logger.info("[shuffle] start Shuffle")
    shuffleHandler.shuffle(workers)
    logger.info("[shuffle] done Shuffle")
  }

  @tailrec
  final def requestMerge(): Unit = {
    logger.info("[requestMerge] Notify shuffle done")

    val response = blockingStub.merge(new MergeRequest(id))
    response.status match {
      case StatusEnum.SUCCESS => {
        logger.info("[requestMerge] Other workers done shuffling too")
      }
      case StatusEnum.FAILED => {
        logger.info("[requestMerge] requestMerge failed.")
        throw new WorkerFailedException
      }
      case _ => {
        /* Wait 5 seconds and retry */
        Thread.sleep(5 * 1000)
        requestMerge
      }
    }
  }

  final def merge(): Unit = {
    // Stop shuffle server
    logger.info("[sort] stop ShuffleHandler Server")
    shuffleHandler.serverStop

    logger.info("[merge] start Merge")
    Merger.merge(tempDir, clientInfo.outputDir, workers(id).subKeyRange)
    FileHandler.getListOfFiles(clientInfo.outputDir).foreach(file => Merger.sortNotTagged(file.getPath))
    logger.info("[merge] done Merge")
  }
}
