package com.microtrack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrackingEvent(
    val traceId: String,
    val serviceName: String,
    val timestamp: Timestamp,
    val checkpointName: String,
    val isError: Boolean,
    val genericData: Any?,
    val successorBy: List<String>?,
    val durationMs: Long
)
