package sorting

import org.scalatest.funsuite.AnyFunSuite
import sorting.Sorter.splitSingleInput
import sorting.TestHelper.isContentsOfSampleAndTestFileEqual

import java.io.File

class SortingTest extends AnyFunSuite {
  val projectPath: String = System.getProperty("user.dir")

  test("Partition single input") {
    val inputPath: String = projectPath + "/src/test/resources/simple/worker/input/sample-input-1"

    val workerPath: String = projectPath + "/src/test/resources/simple/worker"
    val pivots = Map(1 -> Tuple2(" ", "1"), 2 -> Tuple2("2", "5"), 3 -> Tuple2("6", "9"))

    val partitionPath1: String = workerPath + "/partition-1-1-unsorted"
    val partitionPath2: String = workerPath + "/partition-2-1-unsorted"
    val partitionPath3: String = workerPath + "/partition-3-1-unsorted"

    val partitionResultPath1: String = workerPath + "/partition-result-single/partition-1-1-unsorted-test"
    val partitionResultPath2: String = workerPath + "/partition-result-single/partition-2-1-unsorted-test"
    val partitionResultPath3: String = workerPath + "/partition-result-single/partition-3-1-unsorted-test"

    try {
      Sorter.splitSingleInput(inputPath, workerPath + "/partition-", "-1-unsorted", pivots)

      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath1, partitionPath1, new File(partitionResultPath1).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath2, partitionPath2, new File(partitionResultPath2).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath3, partitionPath3, new File(partitionResultPath3).length.toInt))
    } catch {
      case ex: Exception => assert(false)
    }

  }

  test("Partition inputs") {
    val inputPath: String = projectPath + "/src/test/resources/simple/worker/input"

    val workerPath: String = projectPath + "/src/test/resources/simple/worker"
    val pivots = Map(1 -> Tuple2(" ", "1"), 2 -> Tuple2("2", "5"), 3 -> Tuple2("6", "9"))

    val partitionPath1: String = workerPath + "/partition-1-1-unsorted"
    val partitionPath2: String = workerPath + "/partition-2-1-unsorted"
    val partitionPath3: String = workerPath + "/partition-3-1-unsorted"

    val partitionResultPath1: String = workerPath + "/partition-result/partition-1-1-unsorted-test"
    val partitionResultPath2: String = workerPath + "/partition-result/partition-2-1-unsorted-test"
    val partitionResultPath3: String = workerPath + "/partition-result/partition-3-1-unsorted-test"

    try {
      Sorter.partition(inputPath, workerPath, pivots)

      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath1, partitionPath1, new File(partitionResultPath1).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath2, partitionPath2, new File(partitionResultPath2).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath3, partitionPath3, new File(partitionResultPath3).length.toInt))
    } catch {
      case ex: Exception => assert(false)
    }

  }

  test("Sorting file") {
    val unsortedPath: String = projectPath + "/src/test/resources/sorting/sort-sample-unsorted"
    val sortedPath: String = projectPath + "/src/test/resources/sorting/sort-sample"
    val testPath: String = projectPath + "/src/test/resources/sorting/sort-sample-test"
    val sampledFile = new File(unsortedPath)
    val testSampledFile = new File(testPath)


    Sorter.sort(unsortedPath)
    try {
      val isContentsEqual: Boolean = isContentsOfSampleAndTestFileEqual(sortedPath,
        testPath, testSampledFile.length.toInt)
      assert(sampledFile.length == testSampledFile.length)
      assert(isContentsEqual)
    } catch {
      case ex: Exception => assert(false)
    }
  }


}
