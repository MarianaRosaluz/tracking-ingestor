package com.microtrack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class TraceEvent(
    val traceId: UUID,
    val spanId: UUID,
    val parentSpanId: String?,

    val serviceName: String,

    val operationType: OperationType,

    val operationName: String,

    val target: String?,

    val startTime: Instant,
    val endTime: Instant,

    val tags: Map<String, Any>,

    val status: Status
)

enum class OperationType {
    HTTP,
    DATABASE,
    MESSAGE_PUBLISH,
    MESSAGE_CONSUME,
    INTERNAL
}

enum class Status {
    SUCCESS,
    ERROR
}
