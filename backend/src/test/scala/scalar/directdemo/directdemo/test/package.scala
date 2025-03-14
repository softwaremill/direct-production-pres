package scalar.directdemo

import scalar.directdemo.config.Config
import com.softwaremill.quicklens._

import scala.concurrent.duration._

package object test:
  val DefaultConfig: Config = Config.read
  val TestConfig: Config = DefaultConfig.modify(_.email.emailSendInterval).setTo(100.milliseconds)
