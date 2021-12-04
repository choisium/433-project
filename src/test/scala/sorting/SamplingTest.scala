package sorting

import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import scala.io.Source

class SamplingTest extends AnyFunSuite {
  test("Sample file creation from worker's input") {

    val sampleSize = 30;
    val projectPath: String = System.getProperty("user.dir");
    val inputPath: String = projectPath + "/src/test/resources/worker/input"
    val workerPath: String = projectPath + "/src/test/resources/worker"
    val sampledFile = new File(workerPath + "/sample")
    val testSampledFile = new File(projectPath + "/src/test/resources/sorting/sample-test")

    Sampler.sample(inputPath, workerPath, sampleSize)
    try {
      val isContentsEqual: Boolean = isContentsOfSampleAndTestFileEqual(workerPath + "/sample",
        projectPath + "/src/test/resources/sorting/sample-test", sampleSize)
      assert(sampledFile.length == testSampledFile.length)
      assert(isContentsEqual)
    } catch {
      case ex: Exception => assert(false)
    }
  }

  def isContentsOfSampleAndTestFileEqual(samplePath: String, testFilePath: String, sampleSize: Int): Boolean = {
    try {
      val bufferedSample = Source.fromFile(samplePath)
      val bufferedTestFile = Source.fromFile(testFilePath)

      val linesOfSample = bufferedSample.getLines.take(sampleSize);
      val linesOfTestFile = bufferedTestFile.getLines.take(sampleSize);
      val isEqual = linesOfSample.sameElements(linesOfTestFile) && linesOfSample.size.equals(linesOfTestFile.size)

      bufferedSample.close
      bufferedTestFile.close

      isEqual

    } catch {
      case ex: Exception => println(ex)
        false
    }
  }
}
