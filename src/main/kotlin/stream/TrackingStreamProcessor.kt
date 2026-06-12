package com.microtrack.stream

import com.microtrack.model.TraceEvent
import com.microtrack.service.ServiceMetricService
import com.microtrack.service.TraceEventService
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
    private val traceEventService: TraceEventService,
    private val eventSerde: JsonSerde<TraceEvent>
) {

    private val inputTopic = "trace-events"

    @Bean
    fun buildTopology(builder: StreamsBuilder): Topology {
        val eventStream = createEventStream(builder)
        persistTraceEvents(eventStream)
        val aggregatedMetrics = aggregateEventsByServiceAndWindow(eventStream)
        persistAggregatedMetrics(aggregatedMetrics)
        
        return builder.build()
    }

    private fun createEventStream(builder: StreamsBuilder): KStream<String, TraceEvent> {
        return builder.stream(inputTopic, Consumed.with(Serdes.String(), eventSerde))
    }

    private fun persistTraceEvents(stream: KStream<String, TraceEvent>) {
        stream.foreach { _, event ->
            traceEventService.save(event)
        }
    }

    private fun aggregateEventsByServiceAndWindow(
        stream: KStream<String, TraceEvent>
    ): KTable<Windowed<String>, ServiceAgg> {
        val windowSize = Duration.ofMinutes(1)

        return stream
            .filter { _, event -> event.serviceName != null }
            .map { _, event -> KeyValue(event.serviceName!!, event) }
            .groupByKey(Grouped.with(Serdes.String(), eventSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(windowSize))
            .aggregate(
                { ServiceAgg(0L, 0.0, 0L) },
                { _, event, agg -> aggregateEvent(event, agg) },
                Materialized.with(Serdes.String(), JsonSerde(ServiceAgg::class.java))
            )
    }

    private fun aggregateEvent(event: TraceEvent, currentAgg: ServiceAgg): ServiceAgg {
        val newCount = currentAgg.count + 1
        val durationMs = Duration.between(event.startTime, event.endTime).toMillis()
        val newSumDuration = currentAgg.sumDuration + durationMs.toDouble()
        val newCountWithDuration = currentAgg.countWithDuration + 1
        return ServiceAgg(newCount, newSumDuration, newCountWithDuration)
    }

    private fun persistAggregatedMetrics(aggregated: KTable<Windowed<String>, ServiceAgg>) {
        aggregated.toStream().foreach { windowedKey, agg ->
            val serviceName = windowedKey.key()
            val windowStart = Instant.ofEpochMilli(windowedKey.window().start())
            val windowEnd = Instant.ofEpochMilli(windowedKey.window().end())
            val averageDuration = calculateAverageDuration(agg)

            metricService.saveAggregate(serviceName, windowStart, windowEnd, agg.count, averageDuration)
        }
    }

    private fun calculateAverageDuration(agg: ServiceAgg): Double? {
        return if (agg.countWithDuration > 0) {
            agg.sumDuration / agg.countWithDuration
        } else {
            null
        }
    }

    data class ServiceAgg(
        val count: Long,
        val sumDuration: Double,
        val countWithDuration: Long
    )
}
