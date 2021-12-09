package sorting

import org.scalatest.funsuite.AnyFunSuite
import sorting.Sorter.{getListOfStageFiles, splitSingleInput}
import sorting.TestHelper.isContentsOfSampleAndTestFileEqual

import java.io.File

class SortingTest extends AnyFunSuite {
  val projectPath: String = System.getProperty("user.dir")
  /**
   * resources structure:
   * requirements:
   * SHOULD REMOVE "SinglePartitionTest"'s results
   * ~/test/resources/simple/results/partition-result/partition-#-#-test
   *                                                              : to compare with partitioned(unsorted) file
   * ~/test/resources/simple/results/partition-result/partition-#-#-unsorted-test
   *                                                              : to compare with partitioned(sorted) file
   * ~/test/resources/simple/1/input/sample-input-1               : input file
   * ~/test/resources/simple/1/input/sample-input-2               : input file
   *
   * after test:
   * new:
   * ~/test/resources/simple/1/partition-#-#                      : partitioned with pivots, sorted files
   */
  test("Partition inputs and sort") {
    val inputPath: String = projectPath + "/src/test/resources/simple/1/input"

    val workerPath: String = projectPath + "/src/test/resources/simple/1"
    val pivots = Map(1 -> Tuple2(" ", "1"), 2 -> Tuple2("2", "5"), 3 -> Tuple2("6", "9"))

    val partitionPath1: String = workerPath + "/partition-1-1-unsorted"
    val partitionPath2: String = workerPath + "/partition-2-1-unsorted"
    val partitionPath3: String = workerPath + "/partition-3-1-unsorted"

    val partitionResultPath1: String = workerPath + "/../results/partition-result/partition-1-1-unsorted-test"
    val partitionResultPath2: String = workerPath + "/../results/partition-result/partition-2-1-unsorted-test"
    val partitionResultPath3: String = workerPath + "/../results/partition-result/partition-3-1-unsorted-test"

    val sortPath1: String = workerPath + "/partition-1-1"
    val sortPath2: String = workerPath + "/partition-2-1"
    val sortPath3: String = workerPath + "/partition-3-1"

    val sortResultPath1: String = workerPath + "/../results/partition-result/partition-1-1-test"
    val sortResultPath2: String = workerPath + "/../results/partition-result/partition-2-1-test"
    val sortResultPath3: String = workerPath + "/../results/partition-result/partition-3-1-test"

    try {
      Sorter.partition(inputPath, workerPath, pivots)
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath1, partitionPath1, new File(partitionResultPath1).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath2, partitionPath2, new File(partitionResultPath2).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(partitionResultPath3, partitionPath3, new File(partitionResultPath3).length.toInt))

      // get partition-#-#-unsorted files from workerPath, sort each, and save as partition-#-#
      val unsortedPartitionedFiles = getListOfStageFiles(workerPath, "partition")
      for(unsortedPartitionedFile <- unsortedPartitionedFiles){
        Sorter.sort(unsortedPartitionedFile.getPath)
      }
      assert(isContentsOfSampleAndTestFileEqual(sortResultPath1, sortPath1, new File(partitionResultPath1).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(sortResultPath2, sortPath2, new File(partitionResultPath2).length.toInt))
      assert(isContentsOfSampleAndTestFileEqual(sortResultPath3, sortPath3, new File(partitionResultPath3).length.toInt))

    } catch {
      case ex: Exception => assert(false)
    }

  }

  /**
   * resources structure:
   * requirements:
   * ~/test/resources/sorting/sort-sample-test            : to compare with output(sorted) file
   * ~/test/resources/sorting/sort-sample-unsorted        : input(unsorted) file
   * ~/test/resources/sorting/sort-sample-unsorted-test   : copy to "sort-sample-unsorted" before run this test, if needed
   *
   * after test:
   * new:
   * ~/test/resources/sorting/sort-sample                 : output(sorted) file
   * deletion:
   * ~/test/resources/sorting/sort-sample-unsorted        : input(unsorted) file
   */
  test("Sorting file") {
    val unsortedPath: String = projectPath + "/src/test/resources/sorting/sort-sample-unsorted"
    val sortedPath: String = projectPath + "/src/test/resources/sorting/sort-sample"
    val testPath: String = projectPath + "/src/test/resources/sorting/sort-sample-test"
    val sortedFile = new File(sortedPath)
    val testSampledFile = new File(testPath)


    Sorter.sort(unsortedPath)
    try {
      assert(sortedFile.length == testSampledFile.length)
      assert(isContentsOfSampleAndTestFileEqual(sortedPath, testPath, testSampledFile.length.toInt))
    } catch {
      case ex: Exception => assert(false)
    }
  }


}
