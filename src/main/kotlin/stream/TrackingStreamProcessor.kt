package com.microtrack.stream

import com.microtrack.model.TrackingEvent
import com.microtrack.service.ServiceMetricService
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonSerde
import java.time.Duration
import java.time.Instant

@Configuration
class TrackingStreamProcessor(
    private val metricService: ServiceMetricService,
    private val eventSerde: JsonSerde<TrackingEvent>
) {

    private val inputTopic = "microtrack-events"

    @Bean
    fun buildTopology(builder: StreamsBuilder): Topology {
        val serdeString = Serdes.String()

        val stream: KStream<String, TrackingEvent> =
            builder.stream(inputTopic, Consumed.with(serdeString, eventSerde))

        val windowSize = Duration.ofMinutes(1)

        val aggregated: KTable<Windowed<String>, ServiceAgg> = stream
            // Filtra eventos com serviceName nulo
            .filter { _, value -> value.serviceName != null }
            // Usa serviceName como chave
            .map { _, value -> KeyValue(value.serviceName!!, value) }
            .groupByKey(Grouped.with(Serdes.String(), eventSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(windowSize))
            .aggregate(
                { ServiceAgg(0L, 0.0, 0L) },
                { _: String, newValue: TrackingEvent, agg: ServiceAgg ->
                    val count = agg.count + 1
                    val (sumDur, countWithDur) = if (newValue.durationMs != null) {
                        agg.sumDuration + newValue.durationMs.toDouble() to (agg.countWithDuration + 1)
                    } else {
                        agg.sumDuration to agg.countWithDuration
                    }
                    ServiceAgg(count, sumDur, countWithDur)
                },
                Materialized.with(Serdes.String(), JsonSerde(ServiceAgg::class.java))
            )

        aggregated.toStream().foreach { windowedKey, agg ->
            val serviceName = windowedKey.key()
            val windowStart = Instant.ofEpochMilli(windowedKey.window().start())
            val windowEnd = Instant.ofEpochMilli(windowedKey.window().end())
            val avg = if (agg.countWithDuration > 0) agg.sumDuration / agg.countWithDuration else null
            metricService.saveAggregate(serviceName, windowStart, windowEnd, agg.count, avg)
        }

        return builder.build()
    }

    data class ServiceAgg(
        val count: Long,
        val sumDuration: Double,
        val countWithDuration: Long
    )
}
