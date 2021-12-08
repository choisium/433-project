package sorting

import java.io.{BufferedReader, File, FileOutputStream, FileReader}
import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.io.Source

object Sorter {

  // sort file-unsorted and write on file
  def sort(unsortedFilePath: String): Unit = {
    try {
      val bufferedSourceForKey = Source.fromFile(unsortedFilePath)
      val bufferedSource = Source.fromFile(unsortedFilePath)

      val keyAndLineTuples = (for (line <- bufferedSourceForKey.getLines) yield line.substring(0, 10)).toSeq.zipWithIndex
      val sortedKeys = ListMap(keyAndLineTuples.sortWith(_._1 < _._1): _*)

      assert(unsortedFilePath.takeRight(9).equals("-unsorted"))
      for (key <- sortedKeys.keySet) {
        val bufferedReader = new BufferedReader(new FileReader(unsortedFilePath))
        var i = 0
        // find the correct line to write
        while (i < sortedKeys(key)) {
          bufferedReader.readLine
          i += 1
        }
        val line: String = bufferedReader.readLine

        writeOrCreateAndWrite(unsortedFilePath.slice(0, unsortedFilePath.length - 9), line + "\n")
        bufferedSource.close
      }

    } catch {
      case ex: Exception => println(ex)
    }
  }

  // analyze input file and store each lines in partition-destWorkerId-##
  // Map[Int, (String, String)] **
  def partition(inputPath: String, workerPath: String, pivots: Map[Int, Seq[String]]): Any = {
    val listOfInputFiles = getListOfFiles(inputPath)
    for (file <- listOfInputFiles) {
      splitSingleInput(file.getPath, workerPath, pivots)
    }
  }

  def splitSingleInput(inputFile: String, workerPath: String, pivots: Map[Int, Seq[String]]): Any = {
    try {
      val bufferedSourceForKeys = Source.fromFile(inputFile)
      val bufferedSource = Source.fromFile(inputFile)
      val keys = (for (line <- bufferedSourceForKeys.getLines) yield line.substring(0, 10)).toSeq

      for (line <- bufferedSource.getLines.take(inputFile.length)) {
        val key = line.substring(0, 10)
        val destWorker = whereToPut(key, pivots: Map[Int, Seq[String]])
        val writePath: String = workerPath + "/partition-" + destWorker + "-1-unsorted"

        writeOrCreateAndWrite(writePath, line + "\n")
      }
      bufferedSource.close
    } catch {
      case ex: Exception => println(ex)
    }
  }

  def writeOrCreateAndWrite(filePath: String, content: String): Unit = {
    val file = new File(filePath)
    val writer = new FileOutputStream(file, file.exists)
    writer.write(content.getBytes)
  }

  def whereToPut(key: String, ranges: Map[Int, Seq[String]]): String = {
    val properWorker = ranges.filter(range => isInRange(key.toArray, range._2))

    assert(properWorker.size == 1)
    properWorker.keys.head.toString
  }

  def isInRange(key: Array[Char], range: Seq[String]): Boolean = {
    @tailrec
    def _isInRange(key: Array[Char], range: Seq[String]): Boolean = {
      if (range.head.isEmpty) true
      else if (key.head < range.head.head || key.head > range.tail.head.head) false
      else _isInRange(key.tail, Seq(range.head.tail, range.tail.head.tail))
    }

    _isInRange(key, range)
  }

  def getListOfFiles(directoryPath: String): List[File] = {
    val dir = new File(directoryPath)
    if (dir.exists && dir.isDirectory) {
      dir.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }
}



