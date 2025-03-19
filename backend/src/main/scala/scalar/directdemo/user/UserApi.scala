package scalar.directdemo.user

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import scalar.directdemo.Fail
import scalar.directdemo.http.Http.*
import scalar.directdemo.infrastructure.DB
import scalar.directdemo.metrics.Metrics
import scalar.directdemo.security.{ApiKey, Auth}
import scalar.directdemo.http.{EndpointsForDocs, ServerEndpoints}
import scalar.directdemo.util.Strings.{Id, asId}
import sttp.tapir.*

import java.time.Instant
import scala.concurrent.duration.*

class UserApi(auth: Auth[ApiKey], userService: UserService, db: DB, metrics: Metrics)
    extends ServerEndpoints:
  import UserApi._

  // endpoint implementations

  private val registerUserServerEndpoint = registerUserEndpoint.handle { data =>
    val apiKeyResult =
      db.transactEither(userService.registerNewUser(data.login, data.email, data.password))
    metrics.registeredUsersCounter.add(1)
    apiKeyResult.map(apiKey => Register_OUT(apiKey.id.toString))
  }

  private val loginServerEndpoint = loginEndpoint.handle { data =>
    val apiKeyResult =
      db.transactEither(
        userService.login(
          data.loginOrEmail,
          data.password,
          data.apiKeyValidHours.map(h => Duration(h.toLong, HOURS))
        )
      )
    apiKeyResult.map(apiKey => Login_OUT(apiKey.id.toString))
  }

  private def authedEndpoint[I, O](e: Endpoint[Id[ApiKey], I, Fail, O, Any]) =
    e.handleSecurity(authData => auth(authData))

  private val logoutServerEndpoint = authedEndpoint(logoutEndpoint).handleSuccess { _ => data =>
    db.transactEither(Right(userService.logout(data.apiKey.asId[ApiKey])))
    Logout_OUT()
  }

  private val changePasswordServerEndpoint = authedEndpoint(changePasswordEndpoint).handle {
    id => data =>
      val apiKeyResult =
        db.transactEither(userService.changePassword(id, data.currentPassword, data.newPassword))
      apiKeyResult.map(apiKey => ChangePassword_OUT(apiKey.id.toString))
  }

  private val getUserServerEndpoint = authedEndpoint(getUserEndpoint).handle { id => (_: Unit) =>
    val userResult = db.transactEither(userService.findById(id))
    userResult.map(user => GetUser_OUT(user.login, user.emailLowerCase, user.createdOn))
  }

  private val updateUserServerEndpoint = authedEndpoint(updateUserEndpoint).handle { id => data =>
    db.transactEither(userService.changeUser(id, data.login, data.email)).map(_ => UpdateUser_OUT())
  }

  override val endpoints = List(
    registerUserServerEndpoint,
    loginServerEndpoint,
    logoutServerEndpoint,
    changePasswordServerEndpoint,
    getUserServerEndpoint,
    updateUserServerEndpoint
  )
end UserApi

object UserApi extends EndpointsForDocs:
  // endpoint descriptions

  private val UserPath = "user"

  private val registerUserEndpoint = baseEndpoint.post
    .in(UserPath / "register")
    .in(jsonBody[Register_IN])
    .out(jsonBody[Register_OUT])

  private val loginEndpoint = baseEndpoint.post
    .in(UserPath / "login")
    .in(jsonBody[Login_IN])
    .out(jsonBody[Login_OUT])

  private val logoutEndpoint = secureEndpoint[ApiKey].post
    .in(UserPath / "logout")
    .in(jsonBody[Logout_IN])
    .out(jsonBody[Logout_OUT])

  private val changePasswordEndpoint = secureEndpoint[ApiKey].post
    .in(UserPath / "changepassword")
    .in(jsonBody[ChangePassword_IN])
    .out(jsonBody[ChangePassword_OUT])

  private val getUserEndpoint = secureEndpoint[ApiKey].get
    .in(UserPath)
    .out(jsonBody[GetUser_OUT])

  private val updateUserEndpoint = secureEndpoint[ApiKey].post
    .in(UserPath)
    .in(jsonBody[UpdateUser_IN])
    .out(jsonBody[UpdateUser_OUT])

  override val endpointsForDocs = List(
    registerUserEndpoint,
    loginEndpoint,
    logoutEndpoint,
    changePasswordEndpoint,
    getUserEndpoint,
    updateUserEndpoint
  ).map(_.tag("user"))

  //

  case class Register_IN(login: String, email: String, password: String)
      derives ConfiguredJsonValueCodec,
        Schema
  case class Register_OUT(apiKey: String) derives ConfiguredJsonValueCodec, Schema

  case class ChangePassword_IN(currentPassword: String, newPassword: String)
      derives ConfiguredJsonValueCodec,
        Schema
  case class ChangePassword_OUT(apiKey: String) derives ConfiguredJsonValueCodec, Schema

  case class Login_IN(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int])
      derives ConfiguredJsonValueCodec,
        Schema
  case class Login_OUT(apiKey: String) derives ConfiguredJsonValueCodec, Schema

  case class Logout_IN(apiKey: String) derives ConfiguredJsonValueCodec, Schema
  case class Logout_OUT() derives ConfiguredJsonValueCodec, Schema

  case class UpdateUser_IN(login: String, email: String) derives ConfiguredJsonValueCodec, Schema
  case class UpdateUser_OUT() derives ConfiguredJsonValueCodec, Schema

  case class GetUser_OUT(login: String, email: String, createdOn: Instant)
      derives ConfiguredJsonValueCodec,
        Schema
end UserApi
