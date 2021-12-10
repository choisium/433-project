package network

import scala.concurrent.ExecutionContext
import network.NetworkServer

object Master {
  def main(args: Array[String]): Unit = {
    val port: Int = 8000
    require(args.length == 1, "Usage: master [requiredWorkerNum]")
    val server = new NetworkServer(ExecutionContext.global, port, args(0).toInt)
    try {
      server.start
      server.blockUntilShutdown
    } finally {
      server.stop
    }
  }
}