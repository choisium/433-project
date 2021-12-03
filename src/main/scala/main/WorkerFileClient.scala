package network

import scala.concurrent.ExecutionContext
import network.FileClient

object WorkerClient {
  def main(args: Array[String]): Unit = {
    val client = new FileClient("localhost", 50051)
    val filepath = "/Users/choisium/Development/433-project/src/main/resources/result.txt"

    try {
      client.requestShuffle(filepath)
    } finally {
      client.shutdown
    }
  }
}
