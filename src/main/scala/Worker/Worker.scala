package worker

import scala.concurrent.ExecutionContext

object Worker {
  def main(args: Array[String]): Unit = {
    require(args.length == 2, "Usage: worker [server ip] [server port]")
    val client = new NetworkClient(args(0), args(1).toInt)

    try {
      // Connect Phase
      val id = client.connect("localhost", 5001)
      if (id >= 0) {
        println("connect success")
      }
    } finally {
      client.shutdown
    }
  }
}
