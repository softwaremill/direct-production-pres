package scalar.directdemo.travel

import com.augustnagro.magnum.*

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import ox.*
import scalar.directdemo.logging.Logging
import scalar.directdemo.util.Strings.Id
import scalar.directdemo.util.{Clock, IdGenerator}
import scalar.directdemo.infrastructure.Magnum.{*, given}

import sttp.client4.*
import sttp.client4.jsoniter.asJson

import java.time.Instant

class TravelService(backend: SyncBackend, idGenerator: IdGenerator, clock: Clock) extends Logging:
  given JsonValueCodec[List[String]] = JsonCodecMaker.make[List[String]]

  private val citiesRepo = Repo[City, City, Id[City]]
  private val tripsRepo = Repo[Trip, Trip, Id[Trip]]

  def nextTrip()(using DbTx): Trip =
    val cities = citiesRepo.findAll.toList

    val destinationRequest =
      basicRequest
        .post(uri"http://localhost:8070/pick")
        .body(asJson(cities.map(_.name)))
        .response(asStringOrFail)
    val travelModeRequest =
      basicRequest.get(uri"http://localhost:8071/travel_mode/random").response(asStringOrFail)

    val (to, mode) = par(
      destinationRequest.send(backend).body,
      travelModeRequest.send(backend).body
    )

    Trip(idGenerator.nextId(), City("Warsaw"), City(to), mode, clock.now()).tap(tripsRepo.insert)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("cities")
case class City(name: String)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("trips")
case class Trip(id: Id[Trip], start: City, finish: City, mode: String, created: Instant)
