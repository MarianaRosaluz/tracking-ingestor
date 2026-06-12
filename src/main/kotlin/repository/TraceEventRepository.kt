package com.microtrack.repository

import com.microtrack.entity.TraceEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface TraceEventRepository : JpaRepository<TraceEventEntity, Long> {
    fun findByTraceId(traceId: UUID): List<TraceEventEntity>
    fun findByServiceNameAndStartTimeBetween(serviceName: String, startTime: Instant, endTime: Instant): List<TraceEventEntity>
    fun findBySpanId(spanId: UUID): TraceEventEntity?
}
