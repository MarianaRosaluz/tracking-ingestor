package com.microtrack.config

import com.microtrack.model.TraceEvent
import org.apache.kafka.common.serialization.Serdes
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
}
