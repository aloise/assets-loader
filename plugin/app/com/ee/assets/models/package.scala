package com.ee.assets

import com.ee.log._
import play.api.Configuration

package object models {

  private[assets] object Suffix extends Enumeration{
    type Suffix = Value
    val js,css = Value
  }

  /**
   * Config for an asset loader
   * @param concatenate
   * @param minify
   * @param gzip
   * @param deploy - if false then the deployer will not be invoked
   */
  private[assets] case class AssetsLoaderConfig(concatenate: Boolean, minify: Boolean, gzip: Boolean, deploy: Boolean)

  object AssetsLoaderConfig {
    def fromAppConfiguration(mode: String, suffix: Suffix.Suffix, configuration: Configuration): AssetsLoaderConfig = {

      Logger.debug("creating config for: %s, %s".format(mode, suffix))
      Logger.debug(configuration.getConfig("assetsLoader").map(_.toString).getOrElse("Can't find config for assetsLoader"))

      val config = for {
        al <- configuration.getConfig("assetsLoader")
        modeConfig <- al.getConfig(mode)
        suffixConfig <- modeConfig.getConfig(suffix.toString).orElse(Some(modeConfig))
      } yield {
        suffixConfig
      }

      implicit def toBoolean(property: String): Boolean = {
        config.map {
          c =>
            Logger.debug("%s: %s".format(property, c.getBoolean(property)))
            c.getBoolean(property).getOrElse(false)
        }.getOrElse(false)
      }

      AssetsLoaderConfig("concatenate", "minify", "gzip", "deploy")
    }

  }


  case class AssetsInfo(webPath: String, filePath: String)

}

