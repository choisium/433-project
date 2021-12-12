package sorting

import java.io.{BufferedReader, File, FileOutputStream, FileReader}
import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.io.Source
import common.FileHandler


object Sorter {
  // sort file-unsorted and write on file
  def sort(unsortedFilePath: String): Any = {
    try {
      val bufferedSource = Source.fromFile(unsortedFilePath)
      val lines = bufferedSource.getLines.toSeq.sortWith(sortLine(_, _))
      writeLines(unsortedFilePath.dropRight(9), lines)
      bufferedSource.close
      new File(unsortedFilePath).delete
    } catch {
      case ex: Exception => println(ex)
    }
  }

  def sortLine(data1: String, data2: String): Boolean = {
    data1.substring(0, 10) < data2.substring(0, 10)
  }

  // analyze input file and store each lines in partition-destWorkerId-##
  // Map[Int, (String, String)] **
  def partition(inputPaths: Seq[String], workerPath: String, pivots: Map[Int, (String, String)]): Any = {
    for {
      inputPath <- inputPaths
      (file, idx) <- FileHandler.getListOfFiles(inputPath).zipWithIndex
    } {
      splitSingleInput(file.getPath, workerPath + "/partition-", "-" + idx +"-unsorted", pivots)
    }
  }

  def splitSingleInput(inputFile: String, splitTo: String, _pathTail: String, pivots: Map[Int, (String, String)]): Any = {
    try {
      val bufferedSource = Source.fromFile(inputFile)
      val lines = bufferedSource.getLines.toSeq
      val (firstId, firstRange) = pivots.head
      val keyLength = firstRange._1.length

      for {
        (id, range) <- pivots
      } {
        val writePath = splitTo + id + _pathTail
        val partitionLines = lines.filter(line => {
          val lineKey = line.take(keyLength)
          lineKey >= range._1 && lineKey <= range._2
        })
        writeLines(writePath, partitionLines)
      }

      bufferedSource.close
    } catch {
      case ex: Exception => println(ex)
    }
  }

  def writeLines(filePath: String, lines: Seq[String]): Unit = {
    val file = new File(filePath)
    val writer = new FileOutputStream(file, file.exists)
    for (line <- lines) {
      writer.write((line+"\r\n").getBytes)
    }
    writer.close
  }
}



