package network

import scala.concurrent.ExecutionContext
import scala.collection.mutable.Map

import java.util.logging.Logger
import java.io.{File, IOException}

import message.common._
import message.shuffle.{ShuffleGrpc, FileRequest, FileResponse}
import common.{WorkerInfo, FileHandler, loggerLevel}


class ShuffleHandler(serverHost: String, serverPort: Int, id: Int, tempDir: String) {
  val logger = Logger.getLogger(classOf[ShuffleHandler].getName)
  logger.setLevel(loggerLevel.level)

  var server: FileServer = null

  def serverStart(): Unit = {
    server = new FileServer(ExecutionContext.global, serverPort, id, tempDir)
    server.start
  }

  def serverStop(): Unit = {
    if (server != null) {
      server.stop
    }
    server = null
  }

  def shuffle(workers: Map[Int, WorkerInfo]): Unit = {
    /* Rename partition to worker itself */
    for (partitionFile <- FileHandler.getListFilesWithPrefix(tempDir, s"partition-$id-", "")) {
      val shuffleFile = new File(partitionFile.getAbsolutePath.replaceFirst(s"partition-$id-", s"shuffle-$id-"));

      if (shuffleFile.exists())
        throw new IOException("Shuffle file exists");

      if (!partitionFile.renameTo(shuffleFile))
        throw new IOException("Partition file is not renamed")
    }

    /* Send partition to other workers */
    for {
      workerId <- ((id + 1) to workers.size) ++ (1 until id)
    } {
      logger.info(s"[ShuffleHandler] Try to send partition from ${id} to ${workerId}")
      var client: FileClient = null
      try {
        val worker = workers(workerId)
        client = new FileClient(worker.ip, worker.port, id, tempDir)
        client.shuffle(workerId)
      } finally {
        if (client != null) {
          client.shutdown
        }
      }
    }
  }
}