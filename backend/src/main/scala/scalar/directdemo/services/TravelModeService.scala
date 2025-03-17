package scalar.directdemo.services

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import ox.OxApp.Settings
import ox.logback.InheritableMDC
import ox.otel.context.PropagatingVirtualThreadFactory
import ox.{Ox, OxApp, never, sleep}
import scalar.directdemo.infrastructure.SetTraceIdInMDCInterceptor
import scalar.directdemo.logging.Logging
import sttp.tapir.*
import sttp.tapir.server.netty.NettyConfig
import sttp.tapir.server.netty.sync.{NettySyncServer, NettySyncServerOptions}
import sttp.tapir.server.tracing.opentelemetry.OpenTelemetryTracing

import scala.concurrent.duration.*
import scala.util.Random

object TravelModeService extends OxApp.Simple with Logging:
  InheritableMDC.init
  Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

  override protected def settings: Settings = Settings.Default.copy(threadFactory = Some(PropagatingVirtualThreadFactory()))

  override def run(using Ox): Unit =
    val otel = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk

    val serverOptions: NettySyncServerOptions = NettySyncServerOptions.customiseInterceptors
      .prependInterceptor(OpenTelemetryTracing(otel))
      .prependInterceptor(SetTraceIdInMDCInterceptor)
      .options

    val travelModes = List("car", "bike", "walk", "bus", "train")

    val workEndpoint = infallibleEndpoint.get
      .in("travel_mode" / "random")
      .out(stringBody)
      .handleSuccess: _ =>
        sleep(100.millis)
        Random.shuffle(travelModes).head

    NettySyncServer(serverOptions, NettyConfig.default.host("localhost").port(8071)).addEndpoint(workEndpoint).start()

    logger.info(s"Travel mode started")

    never
