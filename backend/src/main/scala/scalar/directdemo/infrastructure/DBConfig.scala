package scalar.directdemo.infrastructure

import scalar.directdemo.config.Sensitive
import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

case class DBConfig(
    username: String,
    password: Sensitive,
    url: String,
    migrateOnStart: Boolean,
    driver: String
) derives ConfigReader
