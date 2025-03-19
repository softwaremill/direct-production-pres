package pres

class S020_OSS_at_SoftwareMill:

  val OSS = List(
    "sttp-client",
    "tapir",
    "magnolia",
    "macwire",
    "ox",
    "elasticmq",
    "quicklens",
    "jox",
    "apache struts"
  )

  val supportedStacks = List(
    "Future" -> List("Scala std", "Twitter"),
    "Functional Effects" -> List("cats-effect", "Monix", "ZIO", "Kyo"),
    "Synchronous",
    "Scala.JS",
    "Scala Native"
  )

  /* We support, and will support, the entire Scala ecosystem. */
