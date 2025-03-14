package scalar.directdemo.metrics

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.{DoubleGauge, LongCounter}

class Metrics(otel: OpenTelemetry):
  private val meter = otel.meterBuilder("directdemo").setInstrumentationVersion("1.0").build()

  lazy val registeredUsersCounter: LongCounter =
    meter
      .counterBuilder("directdemo_registered_users_counter")
      .setDescription("How many users registered on this instance since it was started")
      .build()

  lazy val emailQueueGauge: DoubleGauge =
    meter
      .gaugeBuilder("directdemo_email_queue_gauge")
      .setDescription("How many emails are waiting to be sent")
      .build()
