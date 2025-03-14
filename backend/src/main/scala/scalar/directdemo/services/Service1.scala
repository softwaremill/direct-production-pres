package scalar.directdemo.services

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import ox.OxApp.Settings
import ox.logback.InheritableMDC
import ox.otel.context.PropagatingVirtualThreadFactory
import ox.{Ox, OxApp, never, sleep}
import scalar.directdemo.Main.logger
import scalar.directdemo.infrastructure.SetTraceIdInMDCInterceptor
import scalar.directdemo.logging.Logging
import sttp.tapir.*
import sttp.tapir.server.netty.NettyConfig
import sttp.tapir.server.netty.sync.{NettySyncServer, NettySyncServerOptions}
import sttp.tapir.server.tracing.opentelemetry.OpenTelemetryTracing
import scala.concurrent.duration.*

object Service1 extends OxApp.Simple with Logging:
  InheritableMDC.init
  Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

  override protected def settings: Settings = Settings.Default.copy(threadFactory = PropagatingVirtualThreadFactory())

  override def run(using Ox): Unit =
    val otel = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk

    val serverOptions: NettySyncServerOptions = NettySyncServerOptions.customiseInterceptors
      .prependInterceptor(OpenTelemetryTracing(otel))
      .prependInterceptor(SetTraceIdInMDCInterceptor)
      .options

    val workEndpoint = endpoint.post
      .in("work1")
      .out(stringBody)
      .handle { _ =>
        sleep(1.second)
        Right("ok1")
      }

    NettySyncServer(serverOptions, NettyConfig.default.host("localhost").port(8070)).addEndpoint(workEndpoint).start()

    logger.info(s"Service1 started")

    never
