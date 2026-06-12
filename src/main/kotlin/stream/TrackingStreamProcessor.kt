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
        val eventStream = createEventStream(builder)
        val aggregatedMetrics = aggregateEventsByServiceAndWindow(eventStream)
        persistAggregatedMetrics(aggregatedMetrics)
        
        return builder.build()
    }

    private fun createEventStream(builder: StreamsBuilder): KStream<String, TrackingEvent> {
        return builder.stream(inputTopic, Consumed.with(Serdes.String(), eventSerde))
    }

    private fun aggregateEventsByServiceAndWindow(
        stream: KStream<String, TrackingEvent>
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

    private fun aggregateEvent(event: TrackingEvent, currentAgg: ServiceAgg): ServiceAgg {
        val newCount = currentAgg.count + 1
        val (newSumDuration, newCountWithDuration) = if (event.durationMs != null) {
            currentAgg.sumDuration + event.durationMs.toDouble() to (currentAgg.countWithDuration + 1)
        } else {
            currentAgg.sumDuration to currentAgg.countWithDuration
        }
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
