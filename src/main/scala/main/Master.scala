package network

import scala.concurrent.ExecutionContext
import network.NetworkServer

object Master {
  def main(args: Array[String]): Unit = {
    val port: Int = 8080
    require(args.length == 1, "Usage: master [requiredWorkerNum]")

    val server = new NetworkServer(ExecutionContext.global, port, args(0).toInt)
    server.start
    server.blockUntilShutdown
  }
}