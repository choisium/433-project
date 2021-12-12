package sorting

import org.scalatest.funsuite.AnyFunSuite
import sorting.TestHelper.isContentsOfSampleAndTestFileEqual

import java.io.File

class SamplingTest extends AnyFunSuite {
  test("Sample file creation from worker's input") {

    val sampleSize = 30
    val projectPath: String = System.getProperty("user.dir")
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
}
