package sorting

import org.scalatest.funsuite.AnyFunSuite
import sorting.TestHelper.isContentsOfSampleAndTestFileEqual

import java.io.File

class MergingTest extends AnyFunSuite {
  val projectPath: String = System.getProperty("user.dir")

  test("Merge files") {

  }

  test("Sorts files") {

    val workerPath: String = projectPath + "/src/test/resources/simple/worker"
    val partitionPath1: String = workerPath + "/partition-1-1-unsorted"
    val partitionPath2: String = workerPath + "/partition-2-1-unsorted"
    val partitionPath3: String = workerPath + "/partition-3-1-unsorted"

    try {
      Sorter.sort(partitionPath1)
      Sorter.sort(partitionPath2)
      Sorter.sort(partitionPath3)

    } catch {
      case ex: Exception => assert(false)
    }
  }

}
