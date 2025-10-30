package com.microtrack.repository

import com.microtrack.entity.ServiceMetric
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ServiceMetricRepository : JpaRepository<ServiceMetric, Long> {
    fun findByServiceNameAndWindowStart(serviceName: String, windowStart: Instant): List<ServiceMetric>
}
