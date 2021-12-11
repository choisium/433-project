package common

import scala.collection.mutable
import java.io.{File, IOException}


object FileHandler {
  def getListOfFiles(directoryPath: String): List[File] = {
    val dir = new File(directoryPath)
    if (dir.exists && dir.isDirectory) {
      dir.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def getListOfStageFiles(directoryPath: String, stage: String): List[File] = {
    val dir = new File(directoryPath)
    if (dir.exists && dir.isDirectory) {
      val fileList = dir.listFiles
      fileList.filter(file => file.isFile && file.getName.startsWith(stage)).toList
    } else {
      List[File]()
    }
  }

  def createDir(prefix: String): String = {
    val tempDir = new File(s"${System.getProperty("java.io.tmpdir")}/${prefix}")
    if (!tempDir.mkdir) throw new IOException("Could not create temporary directory: " + tempDir.getAbsolutePath)
    assert(tempDir.isDirectory)
    tempDir.getAbsolutePath
  }

  def createFile(tempDir: String, prefix: String, postfix: String): File = {
    val dir = new File(tempDir)
    assert(dir.isDirectory)
    val tempFile = File.createTempFile(prefix, postfix, dir);
    tempFile
  }

  def getListFilesWithPrefix(directoryPath: String, prefix: String, postfix: String): Seq[File] = {
    val dir = new File(directoryPath)
    if (dir.exists && dir.isDirectory) {
      dir.listFiles
        .filter(file => file.isFile &&
                        file.getName.startsWith(prefix) &&
                        file.getName.endsWith(postfix))
        .toList
    } else {
      List[File]()
    }
  }

  def deleteDir(tempDir: String): Unit = {
    for (file <- getListOfFiles(tempDir)) {
        assert (file.isFile)
        file.delete
    }
    val dir = new File(tempDir)
    dir.delete
    assert(!dir.exists)
  }
}