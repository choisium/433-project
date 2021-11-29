package sorting

import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import scala.io.Source

class SamplingTest extends AnyFunSuite {
  val projectPath: String = System.getProperty("user.dir");
  val workerPath: String = projectPath + "/src/main/resources/1"
  val sampledFile = new File(workerPath + "/sample")
  val testSampledFile = new File(projectPath + "/src/test/scala/sorting/sample-test")

  Sampler.sample(workerPath, 30)
  try {
    val isContentsEqual: Boolean = isContentsOfSampleAndTestFileEqual(workerPath + "/sample", projectPath + "/src/test/scala/sorting/sample-test")
    assert(isContentsEqual)
    assert(sampledFile.length == testSampledFile.length)
  } catch {
    case ex: Exception => assert(false)
  }

  def isContentsOfSampleAndTestFileEqual(samplePath: String, testFilePath: String): Boolean = {
    try {
      val bufferedSample = Source.fromFile(samplePath)
      val bufferedTestFile = Source.fromFile(testFilePath)

      var sampleCount: Int = 0
      val lineOfSample = bufferedSample.getLines();
      val lineOfTestFile = bufferedTestFile.getLines();

      while (lineOfSample.hasNext && lineOfTestFile.hasNext && sampleCount < 30) {
        sampleCount += 1
        if (!lineOfSample.next.equals(lineOfTestFile.next)) return false
      }
      bufferedSample.close
      bufferedTestFile.close
      sampleCount == 30
    } catch {
      case ex: Exception => println(ex)
        false
    }
  }
}
