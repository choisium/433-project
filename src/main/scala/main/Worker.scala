package network

import scala.concurrent.{ExecutionContext, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.control.NonFatal


import java.net._
import java.io.File

import network.{NetworkClient, ClientInfo}


object Worker {
  def main(args: Array[String]): Unit = {
    val clientInfo = parseArguments(args)
    val client = new NetworkClient(clientInfo)

    try {
      // Send ConnectRequest
      client.requestConnect

      // Do Sampling
      client.sample

      // Send SampleRequest
      val samplePromise = Promise[Unit]()
      client.requestSample(samplePromise)
      Await.ready(samplePromise.future, Duration.Inf)

      // Send PivotRequest
      client.requestPivot
      /* TODO: delete println */
      for ((id, w) <- client.workers) {
        println(id, w.keyRange, w.subKeyRange)
      }

      // Do Sorting
      client.sort

      // Send SortRequest
      client.requestSort

      // Do Shuffle
      client.shuffle

      // Send MergeRequest
      client.requestMerge

      // Do Merge
      client.merge

      // Send TerminateRequest with SUCCESS
      client.shutdown(true)
    } catch {
      case ex: Exception => println(ex)
    } finally {
      // Send TerminateRequest with FAILED
      client.shutdown(false)
    }
  }

  def parseArguments(args: Array[String]): ClientInfo = {
    val usage = "Usage: worker [server ip:port] -I [input directory] -O [output directory]"
    require (args.length >= 5, usage)

    require (args(0).contains(":"), usage)
    val masterInfo = args(0).split(":")
    val masterHost = masterInfo(0)
    val masterPort = masterInfo(1).toInt

    require (args(1) == "-I", usage)
    val inputDirs = args.slice(2, args.length - 2)
    require(inputDirs.forall(dirPath => new File(dirPath).isDirectory), "Input directories must be directories")

    require (args(args.length - 2) == "-O" || args(args.length - 3) == "-O", usage)
    var outputDir: String = null
    var fileServerPort: Int = 9000
    if (args(args.length - 2) == "-O") {
      outputDir = args(args.length - 1)
    } else {
      outputDir = args(args.length - 2)
      fileServerPort = args.length - 1
    }

    val dir = new File(outputDir)
    if (!dir.exists) {
      dir.mkdir()
    }

    require(dir.isDirectory, "Output directory must be a directory")

    val fileServerHost = InetAddress.getLocalHost.getHostAddress

    new ClientInfo(masterHost, masterPort, inputDirs, outputDir, fileServerHost, fileServerPort)
  }
}
