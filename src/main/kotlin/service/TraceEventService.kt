package com.microtrack.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.microtrack.entity.TraceEventEntity
import com.microtrack.model.TraceEvent
import com.microtrack.repository.TraceEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TraceEventService(
    private val repository: TraceEventRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun save(traceEvent: TraceEvent) {
        val entity = TraceEventEntity(
            traceId = traceEvent.traceId,
            spanId = traceEvent.spanId,
            parentSpanId = traceEvent.parentSpanId,
            serviceName = traceEvent.serviceName,
            operationType = traceEvent.operationType,
            operationName = traceEvent.operationName,
            target = traceEvent.target,
            startTime = traceEvent.startTime,
            endTime = traceEvent.endTime,
            tags = objectMapper.writeValueAsString(traceEvent.tags),
            status = traceEvent.status
        )
        repository.save(entity)
    }
}
