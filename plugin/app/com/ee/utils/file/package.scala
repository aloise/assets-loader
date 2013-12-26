package com.ee.utils

import _root_.com.ee.log.Logger
import java.io.File
import java.io.FileWriter

package object file {

  def writeToFile(path: String, contents: String): File = {
    val fw = new FileWriter(path)
    fw.write(contents)
    fw.close()
    val newFile = new File(path)
    if (newFile.exists) new File(path) else throw new RuntimeException("Couldn't write the file to: " + path)
  }

  def readContents(f: File): String = {

    Logger.debug("file.readContents: " + f.getName)
    if (f.exists) {
      val source = scala.io.Source.fromFile(f)(io.Codec("UTF-8"))
      val lines = source.mkString
      source.close()
      lines
    }
    else {
      ""
    }
  }

  def recursiveListFiles(f: File): List[File] = f match {
    case doesntExist: File if !f.exists() => {
      Logger.warn("file doesn't exist: " + f.getName)
      List()
    }
    case file: File if f.isFile => List(file)
    case directory: File if f.isDirectory => {
      val files = directory.listFiles.toList
      files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
    case _ => List()
  }

  def distinctFiles(files: File*): List[File] = {
    val withChildren = files.toList.map(recursiveListFiles)
    withChildren.flatten.distinct
  }


  def typeFilter(suffix: String, files: List[File]): List[File] = files.filter(_.getName.endsWith(suffix))

  def nameAndSuffix(f: File): (String, String) = nameAndSuffix(f.getName)

  def nameAndSuffix(s: String): (String, String) = {
    if (s.contains(".")) {
      val lastIndex = s.lastIndexOf(".")
      if (lastIndex == 0) {
        (s, "")
      } else {
        val name = s.substring(0, lastIndex)
        val suffix = s.substring(lastIndex + 1, s.length)
        (name, suffix)
      }
    } else {
      (s, "")
    }
  }

  def fileToString(f: File): String = f.getAbsolutePath + "_" + f.lastModified()

  /** Create a hash from the file list using the name and dateModified.
    */
  def hash(files: List[File],
           fileToString: (File => String) = fileToString): String = {
    val timestamps: String = files
      .sortWith((a: File, b: File) => a.getName < b.getName)
      .map(fileToString)
      .mkString("__")
    timestamps.hashCode().toString
  }

}