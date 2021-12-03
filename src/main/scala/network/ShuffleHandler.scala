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
    val baseDir = s"${System.getProperty("user.dir")}/src/main/resources/$id/partition-"

    for {
      (workerId, worker) <- workers
    } {
      var client: FileClient = null

      try {
        client = new FileClient(worker.ip, worker.port, id)
        println(s"send partition from ${id} to ${workerId}")

        val shufflePromise = Promise[Unit]()
        client.requestShuffle(baseDir+workerId, shufflePromise)
        Await.ready(shufflePromise.future, Duration.Inf)
      } finally {
        if (client != null) {
          client.shutdown
        }
      }
    }
  }
}