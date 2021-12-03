package network

import scala.concurrent.ExecutionContext
import scala.collection.mutable.Map

import java.util.logging.Logger

import io.grpc.Server

import message.common._
import message.shuffle._
import common._
import network.{FileServer, FileClient}

class ShuffleHandler(serverHost: String, serverPort: Int) {
  val logger = Logger.getLogger(classOf[ShuffleHandler].getName)

  var server: FileServer = null

  def serverStart(): Unit = {
    server = new FileServer(ExecutionContext.global, serverPort)
    server.start
  }

  def serverStop(): Unit = {
    if (server != null) {
      server.stop
    }
    server = null
  }

  def shuffle(id: Int, workers: Map[Int, WorkerInfo]): Unit = {
    val clients = for {
      (workerId, worker) <- workers
    } yield {
      workerId -> new FileClient(worker.ip, worker.port)
    }

    try {
      println(clients.size)
    } finally {
      clients.foreach{case (id, client) => client.shutdown}
    }
  }
}