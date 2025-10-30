package com.microtrack.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "service_metrics", indexes = [
    Index(name = "idx_service_window", columnList = "service_name, window_start")
])
data class ServiceMetric(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "service_name", nullable = false)
    val serviceName: String,

    @Column(name = "window_start", nullable = false)
    val windowStart: Instant,

    @Column(name = "window_end", nullable = false)
    val windowEnd: Instant,

    @Column(name = "event_count", nullable = false)
    val eventCount: Long,

    @Column(name = "avg_duration_ms")
    val avgDurationMs: Double? = null
)
