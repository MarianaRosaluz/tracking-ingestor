package com.microtrack.repository

import com.microtrack.entity.LogEventEntity
import com.microtrack.model.LogLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface LogEventRepository : JpaRepository<LogEventEntity, Long> {
    fun findByTraceId(traceId: UUID): List<LogEventEntity>
    fun findByServiceNameAndTimestampBetween(serviceName: String, startTime: Instant, endTime: Instant): List<LogEventEntity>
    fun findByLevel(level: LogLevel): List<LogEventEntity>
    fun findByTraceIdAndLevel(traceId: UUID, level: LogLevel): List<LogEventEntity>
}
