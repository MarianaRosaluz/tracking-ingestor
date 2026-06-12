package com.microtrack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class LogEvent(
    val traceId: UUID,
    val serviceName: String,
    val timestamp: Instant,
    val level: LogLevel,
    val message: String,
    val exception: String?,
    val metadata: Map<String, Any>?
)

enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL
}
