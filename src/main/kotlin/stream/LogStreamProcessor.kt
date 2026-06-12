package com.microtrack.stream

import com.microtrack.model.LogEvent
import com.microtrack.service.LogEventService
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.stereotype.Component

@Component
class LogStreamProcessor(
    private val logEventService: LogEventService,
    private val logEventSerde: JsonSerde<LogEvent>
) {

    private val inputTopic = "log-events"

    fun configureStreams(builder: StreamsBuilder) {
        val logStream = createLogStream(builder)
        persistLogEvents(logStream)
    }

    private fun createLogStream(builder: StreamsBuilder): KStream<String, LogEvent> {
        return builder.stream(inputTopic, Consumed.with(Serdes.String(), logEventSerde))
    }

    private fun persistLogEvents(stream: KStream<String, LogEvent>) {
        stream.foreach { _, event ->
            logEventService.save(event)
        }
    }
}
