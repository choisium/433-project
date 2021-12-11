package sorting

import java.io.{File, PrintWriter}
import scala.io.Source
import common.FileHandler


object Sampler {
  def sample(inputPath: String, workerPath: String, sampleSize: Int): Unit = {
    try {
      val inputFiles = FileHandler.getListOfFiles(inputPath)
      assert(!inputFiles.isEmpty)
      val bufferedSource = Source.fromFile(inputFiles.head)
      val sampleWriter = new PrintWriter(new File(workerPath + "/sample"))

      for (line <- bufferedSource.getLines.take(sampleSize)) {
        sampleWriter.write(line + "\r\n")
      }

      bufferedSource.close
      sampleWriter.close

    } catch {
      case ex: Exception => println(ex)
    }
  }

}
