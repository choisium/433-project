package network

import scala.concurrent.{ExecutionContext, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import network.NetworkClient


object Worker {
  def main(args: Array[String]): Unit = {
    require(args.length >= 2, "Usage: worker [server ip] [server port]")

    var fileServerHost = "localhost"
    var fileServerPort = 9000
    if (args.length == 4) {
      fileServerHost = args(2)
      fileServerPort = args(3).toInt
    }

    val client = new NetworkClient(args(0), args(1).toInt)

    try {
      // Send ConnectRequest
      client.requestConnect(fileServerHost, fileServerPort)

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
      client.sort

      // Send SortRequest
      client.requestSort
      println("SortRequest done. Shuffling start.")

      // Do Shuffle
      client.shuffle

      // Send DoneRequest
      client.requestDone
    } finally {
      client.shutdown
    }
  }
}
