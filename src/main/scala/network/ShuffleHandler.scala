package network

import scala.concurrent.ExecutionContext
import scala.collection.mutable.Map

import java.util.logging.Logger

import message.common._
import message.shuffle.{ShuffleGrpc, FileRequest, FileResponse, SimpleRequest, SimpleResponse}
import common.WorkerInfo


class ShuffleHandler(serverHost: String, serverPort: Int, id: Int, tempDir: String) {
  val logger = Logger.getLogger(classOf[ShuffleHandler].getName)
  var server: FileServer = null

  def serverStart(): Unit = {
    println(s"[ShuffleHandler] file server $serverHost:$serverPort")
    server = new FileServer(ExecutionContext.global, serverPort, id)
    server.start
  }

  def serverStop(): Unit = {
    if (server != null) {
      server.stop
    }
    server = null
  }

  def shuffle(workers: Map[Int, WorkerInfo]): Unit = {
    for {
      workerId <- (id to workers.size) ++ (1 until id)
    } {
      logger.info(s"[ShuffleHandler] Try to send partition from ${id} to ${workerId}")
      var client: FileClient = null
      try {
        val worker = workers(workerId)
        client = new FileClient(worker.ip, worker.port, id)
        logger.info(s"[ShuffleHandler] test")
        logger.info(s"[ShuffleHandler] ${client.test}")
        client.shuffle(tempDir, workerId)
      } finally {
        if (client != null) {
          client.shutdown
        }
      }
    }
  }
}