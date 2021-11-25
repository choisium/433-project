package network

import scala.concurrent.ExecutionContext
import network.NetworkClient


object Worker {
  def main(args: Array[String]): Unit = {
    require(args.length == 2, "Usage: worker [server ip] [server port]")
    val client = new NetworkClient(args(0), args(1).toInt)

    try {
      // Connect Phase
      val res = client.connect("localhost", 5001)
      if (res) {
        // Sampling Phase
      }
    } finally {
      client.shutdown
    }
  }
}
