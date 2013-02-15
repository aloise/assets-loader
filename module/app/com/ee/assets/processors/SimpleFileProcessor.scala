package com.ee.assets.processors

import com.ee.assets.AssetProcessor
import com.ee.assets.models.AssetsLoaderConfig
import com.ee.assets.models.AssetsInfo
import play.api.Play
import play.api.Play.current
import java.io.{FileWriter, File}
import com.ee.utils.string._
import com.ee.utils.file._
import com.ee.js.JavascriptCompiler

class SimpleFileProcessor(info: AssetsInfo, config: AssetsLoaderConfig, targetFolder: String) extends AssetProcessor {

  val ScriptTemplate = """<script type="text/javascript" src="${src}"></script>"""

  def scriptTag(url: String): String = interpolate(ScriptTemplate, ("src", url))

  def process(path: String): String = {

    def concat(files: List[File]): Option[List[File]] = if (config.concatenate) {

      val filesHash = hash(files)
      val newJsFile = path + "-" + filesHash + ".js"
      val destination = targetFolder + info.filePath + "/" + newJsFile

      if (!(new File(destination).exists)) {
        concatFiles(files, destination)
      }
      else {
        println("file already exists: " + new File(destination).getAbsolutePath)
      }
      Some(List(new File(destination)))
    } else {
      Some(files)
    }

    def minify(files: List[File]): Option[List[File]] = if (config.minify) {
      processFilesInList(files, f => com.ee.utils.file.basename(f) + ".min.js", minifyFile)
    } else {
      Some(files)
    }

    def gzip(files: List[File]): Option[List[File]] = if (config.gzip) {
      processFilesInList(files, f => com.ee.utils.file.basename(f) + ".gz.js", gzipFile)
    } else {
      Some(files)
    }

    def processFilesInList(files: List[File], nameFile: (File => String), process: ((File, String) => Unit)): Option[List[File]] =
    {
      val destinationRoot = new File(targetFolder + info.filePath).getAbsolutePath

      val processed = files.filter(isJs).map {
        f: File =>
          val absolutePath = tidyFolders(f.getAbsolutePath)
          val relativePath = absolutePath.replace(destinationRoot, "").replace(f.getName, "")
          val processedName = nameFile(f)
          val destination = targetFolder + info.filePath + "/" + relativePath + "/" + processedName

          if (!(new File(destination).exists)) {
            process(f, destination)
          }
          new File(destination)
      }
      Some(processed)
    }

    val file = Play.getFile(info.filePath + "/" + path)

    def processFileList(files: List[File]): String = {

      val processed: Option[List[File]] = for {
        concatenated <- concat(files)
        minified <- minify(concatenated)
        gzipped <- gzip(minified)
      } yield {
        gzipped
      }

      processed.map {
        files =>
          files.filter(_.getName.endsWith(".js")).map {
            f: File =>
              //TODO: tidy this up

              println("targetFolder: " + targetFolder)
              println("path: " + path)
              println("info.filePath: " + info.filePath)
              println("file: " + file.getAbsolutePath)
              println("this file: " + f.getAbsolutePath)
              println("webPath: " + info.webPath)

              val splitPoint = tidyFolders(targetFolder + info.filePath)
              println("splitPoint: " + splitPoint)
              val thisFilePath = tidyFolders(f.getAbsolutePath)
              val split = thisFilePath.split(splitPoint)
              println("split: " + split.toList.toString)

              val localPath = tidyFolders( info.webPath + "/" + split.last)

              val tidyPath = tidyFolders(path)
              val tidyFile = tidyFolders(file.getAbsolutePath)
              val rootPath: String = tidyFolders(tidyFile.replace(tidyPath, ""))
              val filePath = tidyFolders(thisFilePath.replace(rootPath, ""))
              scriptTag(localPath)
          }.mkString("\n")
      }.getOrElse("")

    }

    val fileList: List[File] = recursiveListFiles(file)
    processFileList(fileList)
  }

  private def concatFiles(files: List[File], destination: String) {
    import com.ee.js.JavascriptCompiler

    val contents = files
      .filter(isJs)
      .map(f => readContents(f)).mkString("\n")

    val out = if (config.minify) JavascriptCompiler.minify(contents, None) else contents
    println(">> write to: " + destination)
    writeToFile(destination, out)
  }

  private def minifyFile(file: File, destination: String) {
    val contents = readContents(file)
    val out = JavascriptCompiler.minify(contents, None)
    println("minifyFile: " + destination)
    writeToFile(destination, out)
  }

  private def gzipFile(file: File, destination: String) {
    val contents = readContents(file)
    println("gzipping: " + destination)
    com.ee.utils.gzip.gzip(contents, destination)
  }

  private def tidyFolders(s: String): String = s.replace("/./", "/").replace("//", "/")

  private def isJs(f: File): Boolean = {
    f.isFile && f.getName.endsWith(".js") && !f.getName.endsWith(".gz.js")
  }

  /** Create a hash from the file list so we can uid it against other files
    */
  private def hash(files: List[File]): String = {

    val timestamps: String = files
      .sortWith((a: File, b: File) => a.getName < b.getName)
      .map(f => f.getAbsolutePath() + "_" + f.lastModified())
      .mkString("__")

    timestamps.hashCode().toString
  }

}