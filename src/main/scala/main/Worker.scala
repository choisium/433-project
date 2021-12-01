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
        println("connect success")

        // // Do Sampling
        // // TODO: workerpath, sampleSize 설정
        // // Sampler에서 inputPath를 받도록 설정(from arguments)
        // Sampler(inputPath, workpath, sampleSize)

        // Send PivotRequest
        val pivotPromise = Promise[Unit]()
        client.pivot(pivotPromise)
        Await.ready(pivotPromise.future, Duration.Inf)
      }
    } finally {
      client.shutdown
    }
  }
}
