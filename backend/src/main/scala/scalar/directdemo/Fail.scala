package scalar.directdemo

/** Base class for all failures in the application. The failures are translated to HTTP API results
  * in the [[scalar.directdemo.http.Http]] class.
  *
  * The class hierarchy is not sealed and can be extended as required by specific functionalities.
  */
abstract class Fail

object Fail:
  case class NotFound(what: String) extends Fail
  case class Conflict(msg: String) extends Fail
  case class IncorrectInput(msg: String) extends Fail
  case class Unauthorized(msg: String) extends Fail
  case object Forbidden extends Fail
