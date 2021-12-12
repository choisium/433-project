package network

import scala.concurrent.ExecutionContext
import java.net._
import network.NetworkServer

object Master {
  def main(args: Array[String]): Unit = {
    require(args.length >= 1 && args(0).toInt > 0, "Usage: master [requiredWorkerNum]")
    val requiredWorkerNum = args(0).toInt
    val port = {
      if (args.length == 2) {
        args(1).toInt
      } else {
        8000
      }
    }

    val server = new NetworkServer(ExecutionContext.global, port, requiredWorkerNum)

    try {
      server.start
      server.blockUntilShutdown
    } catch {
      case ex: Exception => println(ex)
    } finally {
      server.stop
    }
  }
}