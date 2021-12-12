package sorting

import scala.io.Source

object TestHelper {
  def isContentsOfSampleAndTestFileEqual(samplePath: String, testFilePath: String, sampleSize: Int): Boolean = {
    try {
      val bufferedSample = Source.fromFile(samplePath)
      val bufferedTestFile = Source.fromFile(testFilePath)

      val linesOfSample = bufferedSample.getLines.take(sampleSize)
      val linesOfTestFile = bufferedTestFile.getLines.take(sampleSize)
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
