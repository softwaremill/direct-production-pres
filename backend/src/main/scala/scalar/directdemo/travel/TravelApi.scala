package scalar.directdemo.travel

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import scalar.directdemo.http.{EndpointsForDocs, ServerEndpoints}
import scalar.directdemo.http.Http.{baseEndpoint, jsonBody}
import scalar.directdemo.infrastructure.DB
import sttp.tapir.*

class TravelApi(travelService: TravelService, db: DB) extends ServerEndpoints:

  import TravelApi.*

  private val nextTripServerEndpoint = nextTripEndpoint.handleSuccess: data =>
    db.transact:
      val Trip(_, start, finish, mode, _) = travelService.nextTrip()
      Travel_OUT(start.name, finish.name, mode)

  override val endpoints = List(nextTripServerEndpoint)

object TravelApi extends EndpointsForDocs:
  private val nextTripEndpoint = baseEndpoint.post
    .in("travel" / "next")
    .out(jsonBody[Travel_OUT])

  override val endpointsForDocs = List(nextTripEndpoint).map(_.tag("travel"))

  //

  case class Travel_OUT(from: String, to: String, using: String)
      derives ConfiguredJsonValueCodec,
        Schema
