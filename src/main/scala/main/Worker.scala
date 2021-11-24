package network

import scala.concurrent.ExecutionContext
import network.NetworkClient

object Worker {
  def main(args: Array[String]): Unit = {
    require(args.length == 2, "Usage: worker [server ip] [server port]")
    val client = new NetworkClient(args(0), args(1).toInt)
    var id: Int = -1

    try {
      // Connect Phase
      id = client.connect("localhost", 5001)
      if (id >= 0) {
        println("connect success")
      }
    } finally {
      client.shutdown(id)
    }
  }
}
