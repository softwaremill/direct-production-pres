package scalar.directdemo.config

import scalar.directdemo.email.EmailConfig
import scalar.directdemo.http.HttpConfig
import scalar.directdemo.infrastructure.DBConfig
import scalar.directdemo.logging.Logging
import scalar.directdemo.passwordreset.PasswordResetConfig
import scalar.directdemo.user.UserConfig
import scalar.directdemo.version.BuildInfo
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*

import scala.collection.immutable.TreeMap

/** Maps to the `application.conf` file. Configuration for all modules of the application. */
case class Config(
    db: DBConfig,
    api: HttpConfig,
    email: EmailConfig,
    passwordReset: PasswordResetConfig,
    user: UserConfig
) derives ConfigReader

object Config extends Logging:
  def log(config: Config): Unit =
    val baseInfo = s"""
                      |Directdemo configuration:
                      |-----------------------
                      |DB:             ${config.db}
                      |API:            ${config.api}
                      |Email:          ${config.email}
                      |Password reset: ${config.passwordReset}
                      |User:           ${config.user}
                      |
                      |Build & env info:
                      |-----------------
                      |""".stripMargin

    val info = TreeMap(BuildInfo.toMap.toSeq*).foldLeft(baseInfo) { case (str, (k, v)) =>
      str + s"$k: $v\n"
    }

    logger.info(info)
  end log

  def read: Config = ConfigSource.default.loadOrThrow[Config]
