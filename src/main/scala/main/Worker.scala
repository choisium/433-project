package network

import scala.concurrent.{ExecutionContext, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
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
        println("connect success")
        val pivotPromise = Promise[Unit]()
        client.pivot(pivotPromise)
        Await.ready(pivotPromise.future, Duration.Inf)
        Thread.sleep(5 * 1000)
      }
    } finally {
      client.shutdown
    }
  }
}
