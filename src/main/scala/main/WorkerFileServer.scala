package network

import scala.concurrent.ExecutionContext
import network.FileServer

object WorkerServer {
  private val port = 50051

  def main(args: Array[String]): Unit = {
    val server = new FileServer(ExecutionContext.global, port)
    server.start
    server.blockUntilShutdown
  }
}