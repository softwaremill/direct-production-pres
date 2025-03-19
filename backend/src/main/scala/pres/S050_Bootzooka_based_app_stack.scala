package pres

class S050_Bootzooka_based_app_stack:

  val Language = "Scala"
  val DI: Either["MacWire", "Scala"] = Left("MacWire")
  val HTTP = List("Tapir", "Netty", "jsoniter")
  val DB = List("Magnum", "Flyway")
  val Concurrency = "Ox"
  val Observability = List("Logback", "OpenTelemetry")
