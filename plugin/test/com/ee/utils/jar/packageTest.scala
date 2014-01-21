package com.ee.utils.jar

import org.specs2.mutable.{BeforeAfter, Specification}
import java.io.File
import com.ee.utils.file.{recursiveListFiles, relativePath}

class jarPackageTest extends Specification {

  sequential

  class  jartest(val jarContentsPath : String, val outputDirPath:String) extends BeforeAfter {

    lazy val jarDir = new File(jarContentsPath)

    lazy val allFiles = recursiveListFiles(jarDir).map( relativePath(_, jarDir) )

    lazy val jarPath = s"target/${jarDir.getName}.jar"

    lazy val destDir = new File(outputDirPath)

    def before: Any = {
      import scala.sys.process._
      val rmCmd = s"rm -rf ${new File(outputDirPath).getAbsolutePath}"
      val rmJarCmd = s"rm -rf $jarPath"
      val cmd = s"jar cvfM $jarPath -C ${jarDir.getAbsolutePath}/ ."
      println(rmCmd.!!)
      println(rmJarCmd.!!)
      println(cmd.!!)

      val created = outputDirPath.split("/").foldLeft( new File("") ){ (root:File, name: String)  =>
        val f = new File(s"${root.getAbsolutePath}/$name")
        f.mkdir()
        f
      }
      println(s" created: ${created.getAbsolutePath}")
    }

    def after: Any = {

    }
  }

  "extractJar" should {

    "work" in new jartest("test/com/ee/utils/jar/jarOne", "target/tmpJarFolder") {
      extractJar( new File(jarPath), destDir, s => true)

      forall(allFiles){ f =>
        println(f)
        new File( s"$outputDirPath/$f").exists === true
      }
    }
  }

}
