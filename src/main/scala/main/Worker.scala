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
      // Send ConnectRequest
      val res = client.requestConnect("localhost", 5001)
      println("connect success")

      // Do Sampling
      client.sample

      // Send SampleRequest
      val samplePromise = Promise[Unit]()
      client.requestSample(samplePromise)
      Await.ready(samplePromise.future, Duration.Inf)

      // Send PivotRequest
      client.requestPivot
      for ((id, w) <- client.workers) {
        println(id, w.keyRange, w.subKeyRange)
      }

      // Do Sorting

      // Start FileServer

      // Send SortRequest
      client.requestSort
      println("SortRequest done. Shuffling start.")

      // Start FileClient

      // Do Shuffle

      // Send DoneRequest

      Thread.sleep(10 * 1000)
    } finally {
      client.shutdown
    }
  }
}
