package sorting

import org.scalatest.funsuite.AnyFunSuite

import java.io.File

class SamplingTest extends AnyFunSuite {
  Sampler.sample(30)
  val projectPath: String = System.getProperty("user.dir")
  val sampledFile = new File(projectPath + "/src/main/resources/1/sample")
  val testSampledFile = new File(projectPath + "/src/test/scala/sorting/sample-test")

  assert(sampledFile.length == testSampledFile.length)
}
