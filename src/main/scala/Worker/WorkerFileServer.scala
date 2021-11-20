package worker

import scala.concurrent.ExecutionContext

object WorkerServer {
  private val port = 50051

  def main(args: Array[String]): Unit = {
    val server = new FileServer(ExecutionContext.global, port)
    server.start
    server.blockUntilShutdown
  }
}