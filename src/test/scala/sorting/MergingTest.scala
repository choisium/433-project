package sorting

import org.scalatest.funsuite.AnyFunSuite
import sorting.TestHelper.isContentsOfSampleAndTestFileEqual

import java.io.File

class MergingTest extends AnyFunSuite {
  val projectPath: String = System.getProperty("user.dir")

  /**
   * resources structure:
   * requirements:
   * ~/test/resources/simple/results/shuffle-result/output-#      : to compare with merged file
   * ~/test/resources/simple/1/shuffle-#-#                        : input files, if workerId is equal to first #
   *
   * after test:
   * new:
   * ~/test/resources/simple/1/output/output-#                    : output files
   */
  test("Merge files") {
    val workerPath: String = projectPath + "/src/test/resources/simple/1"
    val outputPath: String = workerPath + "/output"
    val mergeResultPath: String = workerPath + "/../results/shuffle-result"

    val mergePath0 = workerPath + "/output/output-0"
    val mergePath1 = workerPath + "/output/output-1"
    val mergePath2 = workerPath + "/output/output-2"

    val mergeResultPath0 = workerPath + "/../results/shuffle-result/output-0"
    val mergeResultPath1 = workerPath + "/../results/shuffle-result/output-1"
    val mergeResultPath2 = workerPath + "/../results/shuffle-result/output-2"

    // keys start with 0 ~ 9
    val subRanges = Seq(Tuple2(" ", "3"), Tuple2("4", "7"), Tuple2("8", "9"))

    try {
      Merger.merge(workerPath, outputPath, subRanges)
      assert(isContentsOfSampleAndTestFileEqual(mergeResultPath0, mergePath0, new File(mergeResultPath0).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(mergeResultPath1, mergePath1, new File(mergeResultPath1).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(mergeResultPath2, mergePath2, new File(mergeResultPath2).length.toInt))
    } catch {
      case ex: Exception => assert(false)
    }
  }

}
