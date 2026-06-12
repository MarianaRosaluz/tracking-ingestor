package com.microtrack.config

import com.microtrack.model.LogEvent
import com.microtrack.model.TraceEvent
import com.microtrack.stream.LogStreamProcessor
import com.microtrack.stream.TrackingStreamProcessor
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.support.serializer.JsonSerde

@Configuration
@EnableKafkaStreams
class KafkaStreamsConfig {

    @Value("\${spring.kafka.streams.application-id}")
    private lateinit var appId: String

    @Value("\${spring.kafka.streams.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean(name = ["defaultKafkaStreamsConfig"])
    fun kafkaStreamsConfig(): KafkaStreamsConfiguration {
        val props = mapOf(
            "application.id" to appId,
            "bootstrap.servers" to bootstrapServers,
            "default.key.serde" to Serdes.String().javaClass.name,
            "default.value.serde" to JsonSerde::class.java.name
        )
        return KafkaStreamsConfiguration(props)
    }

    @Bean
    fun trackingEventSerde(): JsonSerde<TraceEvent> {
        val serde = JsonSerde(TraceEvent::class.java)
        serde.deserializer().addTrustedPackages("*")
        return serde
    }

    @Bean
    fun logEventSerde(): JsonSerde<LogEvent> {
        val serde = JsonSerde(LogEvent::class.java)
        serde.deserializer().addTrustedPackages("*")
        return serde
    }

    @Bean
    fun kafkaStreamsTopology(
        builder: StreamsBuilder,
        trackingStreamProcessor: TrackingStreamProcessor,
        logStreamProcessor: LogStreamProcessor
    ): Topology {
        trackingStreamProcessor.configureStreams(builder)
        logStreamProcessor.configureStreams(builder)
        return builder.build()
    }
}
