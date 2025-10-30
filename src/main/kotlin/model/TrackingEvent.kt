package com.microtrack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrackingEvent(
    val traceId: String?,
    val serviceName: String,
    val timestamp: Instant,
    val status: String?,
    val durationMs: Long? = null,
    val metadata: Map<String, Any>? = null
)
