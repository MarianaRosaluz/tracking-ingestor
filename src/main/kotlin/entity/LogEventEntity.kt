package com.microtrack.entity

import com.microtrack.model.LogLevel
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "log_events", indexes = [
    Index(name = "idx_log_trace_id", columnList = "trace_id"),
    Index(name = "idx_log_service_time", columnList = "service_name, timestamp"),
    Index(name = "idx_log_level", columnList = "level")
])
data class LogEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "trace_id", nullable = false)
    val traceId: UUID,

    @Column(name = "service_name", nullable = false)
    val serviceName: String,

    @Column(name = "timestamp", nullable = false)
    val timestamp: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    val level: LogLevel,

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(name = "exception", columnDefinition = "TEXT")
    val exception: String?,

    @Type(JsonBinaryType::class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    val metadata: String?
)
