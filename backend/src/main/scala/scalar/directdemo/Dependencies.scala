package scalar.directdemo

import scalar.directdemo.admin.VersionApi
import scalar.directdemo.config.Config
import scalar.directdemo.email.EmailService
import scalar.directdemo.email.sender.EmailSender
import scalar.directdemo.http.{HttpApi, HttpConfig}
import scalar.directdemo.infrastructure.DB
import scalar.directdemo.metrics.Metrics
import scalar.directdemo.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import scalar.directdemo.security.{ApiKeyAuthToken, ApiKeyService, Auth}
import scalar.directdemo.user.UserApi
import scalar.directdemo.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator}
import com.softwaremill.macwire.{autowire, autowireMembersOf}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import io.opentelemetry.instrumentation.runtimemetrics.java8.{
  Classes,
  Cpu,
  GarbageCollector,
  MemoryPools,
  Threads
}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import ox.{Ox, discard, tap, useCloseableInScope, useInScope}
import scalar.directdemo.travel.{StreamToKafkaService, TravelApi}
import sttp.client4.SyncBackend
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.client4.logging.slf4j.Slf4jLoggingBackend
import sttp.client4.opentelemetry.{OpenTelemetryMetricsBackend, OpenTelemetryTracingSyncBackend}
import sttp.tapir.AnyEndpoint

case class Dependencies(
    httpApi: HttpApi,
    emailService: EmailService,
    streamToKafkaService: StreamToKafkaService
)

object Dependencies:
  val endpointsForDocs: List[AnyEndpoint] =
    List(UserApi, PasswordResetApi, VersionApi, TravelApi).flatMap(_.endpointsForDocs)

  private case class Apis(
      userApi: UserApi,
      passwordResetApi: PasswordResetApi,
      versionApi: VersionApi,
      travelApi: TravelApi
  ):
    def endpoints = List(userApi, passwordResetApi, versionApi, travelApi).flatMap(_.endpoints)

  def create(using Ox): Dependencies =
    val config = Config.read.tap(Config.log)
    val otel = initializeOtel()
    val sttpBackend = useInScope(
      Slf4jLoggingBackend(
        OpenTelemetryMetricsBackend(
          OpenTelemetryTracingSyncBackend(HttpClientSyncBackend(), otel),
          otel
        )
      )
    )(_.close())
    val db: DB = useCloseableInScope(DB.createTestMigrate(config.db))

    create(config, otel, sttpBackend, db, DefaultClock)

  /** Create the service graph using the given infrastructure services & configuration. */
  def create(
      config: Config,
      otel: OpenTelemetry,
      sttpBackend: SyncBackend,
      db: DB,
      clock: Clock
  ): Dependencies =
    autowire[Dependencies](
      autowireMembersOf(config),
      otel,
      sttpBackend,
      db,
      DefaultIdGenerator,
      clock,
      EmailSender.create,
      (apis: Apis, otel: OpenTelemetry, httpConfig: HttpConfig) =>
        new HttpApi(apis.endpoints, Dependencies.endpointsForDocs, otel, httpConfig),
      classOf[EmailService],
      new Auth(_: ApiKeyAuthToken, _: DB, _: Clock),
      new Auth(_: PasswordResetAuthToken, _: DB, _: Clock)
    )

  private def initializeOtel(): OpenTelemetry =
    AutoConfiguredOpenTelemetrySdk
      .initialize()
      .getOpenTelemetrySdk()
      .tap { otel =>
        // see https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry/runtime-telemetry-java8/library
        Classes.registerObservers(otel)
        Cpu.registerObservers(otel)
        MemoryPools.registerObservers(otel)
        Threads.registerObservers(otel)
        GarbageCollector.registerObservers(otel).discard
      }
      .tap(OpenTelemetryAppender.install)
