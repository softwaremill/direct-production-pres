package scalar.directdemo.admin

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import scalar.directdemo.http.Http.*
import scalar.directdemo.version.BuildInfo
import sttp.shared.Identity
import sttp.tapir.Schema
import sttp.tapir.server.ServerEndpoint
import scalar.directdemo.http.EndpointsForDocs
import scalar.directdemo.http.ServerEndpoints

/** Defines an endpoint which exposes the current application version information. */
class VersionApi extends ServerEndpoints:
  import VersionApi._

  private val versionServerEndpoint: ServerEndpoint[Any, Identity] = versionEndpoint.handleSuccess {
    _ =>
      Version_OUT(BuildInfo.lastCommitHash)
  }

  override val endpoints = List(versionServerEndpoint)

object VersionApi extends EndpointsForDocs:
  private val AdminPath = "admin"

  private val versionEndpoint = baseEndpoint.get
    .in(AdminPath / "version")
    .out(jsonBody[Version_OUT])

  override val endpointsForDocs = List(versionEndpoint).map(_.tag("admin"))

  case class Version_OUT(buildSha: String) derives ConfiguredJsonValueCodec, Schema
