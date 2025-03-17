package scalar.directdemo.services

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import ox.OxApp.Settings
import ox.logback.InheritableMDC
import ox.otel.context.PropagatingVirtualThreadFactory
import ox.{Ox, OxApp, never, sleep}
import scalar.directdemo.infrastructure.SetTraceIdInMDCInterceptor
import scalar.directdemo.logging.Logging
import sttp.tapir.*
import sttp.tapir.json.jsoniter.jsonBody
import sttp.tapir.server.netty.NettyConfig
import sttp.tapir.server.netty.sync.{NettySyncServer, NettySyncServerOptions}
import sttp.tapir.server.tracing.opentelemetry.OpenTelemetryTracing

import scala.concurrent.duration.*
import scala.util.Random

object PickRandomService extends OxApp.Simple with Logging:
  InheritableMDC.init
  Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

  override protected def settings: Settings = Settings.Default.copy(threadFactory = Some(PropagatingVirtualThreadFactory()))

  given JsonValueCodec[List[String]] = JsonCodecMaker.make[List[String]]

  override def run(using Ox): Unit =
    val otel = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk

    val serverOptions: NettySyncServerOptions = NettySyncServerOptions.customiseInterceptors
      .prependInterceptor(OpenTelemetryTracing(otel))
      .prependInterceptor(SetTraceIdInMDCInterceptor)
      .options

    val pickRandomEndpoint = infallibleEndpoint.post
      .in("pick")
      .in(jsonBody[List[String]])
      .out(stringBody)
      .handleSuccess: choices =>
        sleep(50.millis)
        Random.shuffle(choices).head

    NettySyncServer(serverOptions, NettyConfig.default.host("localhost").port(8070)).addEndpoint(pickRandomEndpoint).start()

    logger.info(s"Pick random started")

    never
