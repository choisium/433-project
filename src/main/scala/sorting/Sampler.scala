package sorting

import org.json4s.JsonDSL.int2jvalue

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.control.Breaks.break

object Sampler {
  def sample(workerPath: String, sampleSize: Int): Unit = {
    try {
      val bufferedSource = Source.fromFile(workerPath + "/input/input-1")
      val sampleWriter = new PrintWriter(new File(workerPath + "/sample"))

      var sampleCount: Int = 0
      val lineOfInput = bufferedSource.getLines();

      while (lineOfInput.hasNext && sampleCount < sampleSize) {
        sampleWriter.write(lineOfInput.next() + "\n")
        sampleCount += 1
      }

      bufferedSource.close
      sampleWriter.close()

    } catch {
      case ex: Exception => println(ex)
    }
  }

}
