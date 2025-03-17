package scalar.directdemo.travel

import com.augustnagro.magnum.{TableInfo, sql}
import org.apache.kafka.clients.producer.ProducerRecord
import ox.*
import ox.flow.Flow
import ox.kafka.{KafkaDrain, ProducerSettings}
import scalar.directdemo.infrastructure.DB
import scalar.directdemo.logging.Logging
import scalar.directdemo.util.Clock
import scalar.directdemo.util.Strings.Id
import scalar.directdemo.infrastructure.Magnum.{*, given}

import scala.concurrent.duration.DurationInt

class StreamToKafkaService(db: DB, clock: Clock) extends Logging:

  private val trips = TableInfo[Trip, Trip, Id[Trip]]

  def start()(using Ox): Unit =
    fork(run())
    logger.info("Starting streaming of new trips Kafka")

  private def run(): Unit =
    val start = clock.now()
    val settings = ProducerSettings.default.bootstrapServers("localhost:9092")

    Flow
      .tick(1.second)
      .mapStateful(clock.now()) { (last, _) =>
        val newTrips = db.transact {
          sql"""SELECT ${trips.all} FROM $trips WHERE ${trips.created} > $last""".query[Trip].run()
        }

        val newS = newTrips.map(_.created).maxOption.getOrElse(last)
        (newS, newTrips)
      }
      .mapConcat(identity)
      .map(trip => s"${trip.id},${trip.start.name},${trip.finish.name},${trip.mode}")
      .map(msg => ProducerRecord[String, String]("trips", msg))
      .pipe(KafkaDrain.runPublish(settings))
