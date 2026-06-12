package com.microtrack.repository

import com.microtrack.entity.TraceEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface TraceEventRepository : JpaRepository<TraceEventEntity, Long> {
    fun findByTraceId(traceId: String): List<TraceEventEntity>
    fun findByServiceNameAndStartTimeBetween(serviceName: String, startTime: Instant, endTime: Instant): List<TraceEventEntity>
    fun findBySpanId(spanId: String): TraceEventEntity?
}
