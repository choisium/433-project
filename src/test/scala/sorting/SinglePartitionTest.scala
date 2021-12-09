package sorting

import org.scalatest.funsuite.AnyFunSuite
import sorting.Sorter.getListOfStageFiles
import sorting.TestHelper.isContentsOfSampleAndTestFileEqual

import java.io.File

class SinglePartitionTest extends AnyFunSuite {
  val projectPath: String = System.getProperty("user.dir")

  /**
   * resources structure:
   * requirements:
   * ~/test/resources/simple/1/input/sample-input-1                       : input file
   * ~/test/resources/simple/results/partition-result-single/
   *                                      partition-#-#-unsorted-test     : to compare with partitioned file
   *
   * after test:
   * new:
   * ~/test/resources/simple/1/partition-#-#-unsorted                     : partitioned with pivots, unsorted files
   */
  test("Partition single input") {
    val inputPath: String = projectPath + "/src/test/resources/simple/1/input/sample-input-1"

    val workerPath: String = projectPath + "/src/test/resources/simple/1"
    val pivots = Map(1 -> Tuple2(" ", "1"), 2 -> Tuple2("2", "5"), 3 -> Tuple2("6", "9"))

    val partitionPath1: String = workerPath + "/partition-1-1-unsorted"
    val partitionPath2: String = workerPath + "/partition-2-1-unsorted"
    val partitionPath3: String = workerPath + "/partition-3-1-unsorted"

    val partitionResultPath1: String = workerPath + "/../results/partition-result-single/partition-1-1-unsorted-test"
    val partitionResultPath2: String = workerPath + "/../results/partition-result-single/partition-2-1-unsorted-test"
    val partitionResultPath3: String = workerPath + "/../results/partition-result-single/partition-3-1-unsorted-test"

    try {
      Sorter.splitSingleInput(inputPath, workerPath + "/partition-", "-1-unsorted", pivots)

      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath1, partitionPath1, new File(partitionResultPath1).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath2, partitionPath2, new File(partitionResultPath2).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath3, partitionPath3, new File(partitionResultPath3).length.toInt))
    } catch {
      case ex: Exception => assert(false)
    }
  }
}
