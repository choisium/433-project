package sorting

import common.FileHandler
import sorting.Sorter.{sort, splitSingleInput}

import java.io.File
import scala.util.Try

object Merger {

  // split and merge ~/shuffle-workerId-## files with subRanges
  // merge shuffled files into output-##
  def merge(workerPath: String, outputPath: String, subRanges: Seq[(String, String)]): Any = {
    val listOfInputFiles = FileHandler.getListOfStageFiles(workerPath, "shuffle-")
    for (file <- listOfInputFiles) {
      splitSingleInput(file.getPath, outputPath + "/output.", "", Iterator.from(0).zip(subRanges).toMap)
    }
  }

  def sortNotTagged(unsortedFilePath: String): Any = {
    tagAsUnsorted(unsortedFilePath)
    sort(unsortedFilePath + "-unsorted")
  }

  def tagAsUnsorted(oldName: String): Boolean =
    Try(new File(oldName)
      .renameTo(new File(oldName + "-unsorted")))
      .getOrElse(false)
}
