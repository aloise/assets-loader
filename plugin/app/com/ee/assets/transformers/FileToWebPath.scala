package com.ee.assets.transformers

import com.ee.assets.models.AssetsInfo
import com.ee.log.Logger

class FileToWebPath(info: AssetsInfo) extends Transformer[String,String] {

  lazy val logger = Logger("file-to-web")

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    elements.map {
      e =>
        Element[String](toWebPath(e.path), None)
    }
  }

  private def toWebPath(p: String): String = {

    if (!p.contains(info.filePath)) {
      logger.warn(s"$p doesn't contain ${info.filePath} - so nothing to replace")
    }
    val out = p.replace(info.filePath, info.webPath)

    if(out.startsWith("/")) out else s"/$out"
  }
}
