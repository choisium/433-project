package network

import scala.concurrent.{ExecutionContext, Promise, Await}
import scala.concurrent.duration._
import scala.collection.mutable.Map

import java.util.logging.Logger

import io.grpc.Server

import message.common._
import message.shuffle._
import common._
import network.{FileServer, FileClient}

class ShuffleHandler(serverHost: String, serverPort: Int, id: Int) {
  val logger = Logger.getLogger(classOf[ShuffleHandler].getName)
  var server: FileServer = null

  def serverStart(): Unit = {
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
    val baseDir = s"${System.getProperty("user.dir")}/src/main/resources/$id"

    for {
      workerId <- (id to workers.size) ++ (1 until id)
    } {
      logger.info(s"[ShuffleHandler] Try to send partition from ${id} to ${workerId}")
      var client: FileClient = null
      try {
        val worker = workers(workerId)
        client = new FileClient(worker.ip, worker.port, id)
        client.shuffle(baseDir, workerId)
      } finally {
        if (client != null) {
          client.shutdown
        }
      }
    }
  }
}