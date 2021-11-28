package sorting

import org.json4s.JsonDSL.int2jvalue

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.control.Breaks.break

object Sampler {

  /* file reading example */

  def sample(sampleSize: Int): Unit = {
    try {
      val projectPath: String = System.getProperty("user.dir")
      val bufferedSource = Source.fromFile(projectPath + "/src/main/resources/1/input/input-01")
      val sampleWriter = new PrintWriter(new File(projectPath + "/src/main/resources/1/sample"))

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
