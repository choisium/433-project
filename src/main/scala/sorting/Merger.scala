package sorting

import sorting.Sorter.{sort, splitSingleInput}

import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}
import scala.util.Try

object Merger {

  // split and merge ~/shuffle-workerId-## files with subRanges (unsorted)
  // merge shuffled files into output-##
  def merge(workerPath: String, subRanges: Seq[(String, String)]): Any = {
    val listOfInputFiles = getListOfStageFiles(workerPath, "shuffle")
    for (file <- listOfInputFiles) {
      splitSingleInput(file.getPath, workerPath + "/output/output-", " ", Iterator.from(0).zip(subRanges).toMap)
    }
  }

  def sortNotTagged(unsortedFilePath: String): Any = {
    tagAsUnsorted(unsortedFilePath)
    sort(unsortedFilePath + "-unsorted")
  }

  def tagAsUnsorted(oldName: String): Boolean = Try(new File(oldName).renameTo(new File(oldName + "-unsorted"))).getOrElse(false)

  def getListOfStageFiles(directoryPath: String, stage: String): List[File] = {
    val dir = new File(directoryPath)
    if (dir.exists && dir.isDirectory) {
      val fileList = dir.listFiles
      fileList.filter(file => file.isFile && file.getName.substring(0, stage.length).equals(stage)).toList
    } else {
      List[File]()
    }
  }


  // move partition-workerId-## files to each worker's shuffle directory with name of ~/shuffle-workerId-##
  // on network
  // for test
  def shuffle(workerPath: String): Any = {
    val listOfPartitionedFiles = getListOfStageFiles(workerPath, "partition")
    for (file <- listOfPartitionedFiles) {
      // under 10 workers exists
      val workerId = file.getPath.substring(0, 11).takeRight(1)
      val path = Files.move(
        Paths.get(file.getPath),
        Paths.get(workerPath + workerId + "/shuffle-" + workerId + "-1"),
        StandardCopyOption.REPLACE_EXISTING)
    }
  }
}
