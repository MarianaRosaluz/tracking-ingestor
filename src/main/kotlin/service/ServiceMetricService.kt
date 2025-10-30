package com.microtrack.service

import com.microtrack.entity.ServiceMetric
import com.microtrack.repository.ServiceMetricRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ServiceMetricService(private val repository: ServiceMetricRepository) {

    @Transactional
    fun saveAggregate(
        serviceName: String,
        windowStart: Instant,
        windowEnd: Instant,
        count: Long,
        avgDurationMs: Double?
    ) {
        val metric = ServiceMetric(
            serviceName = serviceName,
            windowStart = windowStart,
            windowEnd = windowEnd,
            eventCount = count,
            avgDurationMs = avgDurationMs
        )
        repository.save(metric)
    }
}
