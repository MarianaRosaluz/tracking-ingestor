package com.microtrack.entity

import com.microtrack.model.OperationType
import com.microtrack.model.Status
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "trace_events", indexes = [
    Index(name = "idx_trace_id", columnList = "trace_id"),
    Index(name = "idx_service_time", columnList = "service_name, start_time"),
    Index(name = "idx_span_id", columnList = "span_id")
])
data class TraceEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "trace_id", nullable = false)
    val traceId: UUID,

    @Column(name = "span_id", nullable = false)
    val spanId: UUID,

    @Column(name = "parent_span_id")
    val parentSpanId: String?,

    @Column(name = "service_name", nullable = false)
    val serviceName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    val operationType: OperationType,

    @Column(name = "operation_name", nullable = false)
    val operationName: String,

    @Column(name = "target")
    val target: String?,

    @Column(name = "start_time", nullable = false)
    val startTime: Instant,

    @Column(name = "end_time", nullable = false)
    val endTime: Instant,

    @Type(JsonBinaryType::class)
    @Column(name = "tags", columnDefinition = "jsonb")
    val tags: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: Status
)
