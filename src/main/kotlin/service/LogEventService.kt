package com.microtrack.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.microtrack.entity.LogEventEntity
import com.microtrack.model.LogEvent
import com.microtrack.repository.LogEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LogEventService(
    private val repository: LogEventRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun save(logEvent: LogEvent) {
        val entity = LogEventEntity(
            traceId = logEvent.traceId,
            serviceName = logEvent.serviceName,
            timestamp = logEvent.timestamp,
            level = logEvent.level,
            message = logEvent.message,
            exception = logEvent.exception,
            metadata = logEvent.metadata?.let { objectMapper.writeValueAsString(it) }
        )
        repository.save(entity)
    }
}
