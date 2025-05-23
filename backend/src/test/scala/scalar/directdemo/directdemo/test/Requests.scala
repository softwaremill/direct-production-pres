package scalar.directdemo.test

import com.github.plokhotnyuk.jsoniter_scala.core.writeToString
import scalar.directdemo.passwordreset.PasswordResetApi.{ForgotPassword_IN, PasswordReset_IN}
import scalar.directdemo.user.UserApi.*
import sttp.client3.{Response, SttpBackend, UriContext, basicRequest}
import sttp.shared.Identity

import scala.util.Random

class Requests(backend: SttpBackend[Identity, Any]) extends TestSupport:
  private val random = new Random()

  def randomLoginEmailPassword(): (String, String, String) =
    (random.nextString(12), s"user${random.nextInt(9000)}@directdemo.com", random.nextString(12))

  private val basePath = "http://localhost:8080/api/v1"

  def registerUser(login: String, email: String, password: String): Response[Either[String, String]] =
    basicRequest
      .post(uri"$basePath/user/register")
      .body(writeToString(Register_IN(login, email, password)))
      .send(backend)

  def newRegisteredUsed(): RegisteredUser =
    val (login, email, password) = randomLoginEmailPassword()
    val apiKey = registerUser(login, email, password).body.shouldDeserializeTo[Register_OUT].apiKey
    RegisteredUser(login, email, password, apiKey)

  def loginUser(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int] = None): Response[Either[String, String]] =
    basicRequest
      .post(uri"$basePath/user/login")
      .body(writeToString(Login_IN(loginOrEmail, password, apiKeyValidHours)))
      .send(backend)

  def logoutUser(apiKey: String): Response[Either[String, String]] =
    basicRequest
      .post(uri"$basePath/user/logout")
      .body(writeToString(Logout_IN(apiKey)))
      .header("Authorization", s"Bearer $apiKey")
      .send(backend)

  def getUser(apiKey: String): Response[Either[String, String]] =
    basicRequest
      .get(uri"$basePath/user")
      .header("Authorization", s"Bearer $apiKey")
      .send(backend)

  def changePassword(apiKey: String, password: String, newPassword: String): Response[Either[String, String]] =
    basicRequest
      .post(uri"$basePath/user/changepassword")
      .body(writeToString(ChangePassword_IN(password, newPassword)))
      .header("Authorization", s"Bearer $apiKey")
      .send(backend)

  def updateUser(apiKey: String, login: String, email: String): Response[Either[String, String]] =
    basicRequest
      .post(uri"$basePath/user")
      .body(writeToString(UpdateUser_IN(login, email)))
      .header("Authorization", s"Bearer $apiKey")
      .send(backend)

  def forgotPassword(loginOrEmail: String): Response[Either[String, String]] =
    basicRequest
      .post(uri"$basePath/passwordreset/forgot")
      .body(writeToString(ForgotPassword_IN(loginOrEmail)))
      .send(backend)

  def resetPassword(code: String, password: String): Response[Either[String, String]] =
    basicRequest
      .post(uri"$basePath/passwordreset/reset")
      .body(writeToString(PasswordReset_IN(code, password)))
      .send(backend)
