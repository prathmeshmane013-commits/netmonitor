package com.netmonitor.repository;

import com.netmonitor.model.HealthCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface HealthCheckResultRepository extends JpaRepository<HealthCheckResult, Long> {

    List<HealthCheckResult> findByMonitoredServiceIdOrderByCheckedAtDesc(Long serviceId);

    @Query("SELECT h FROM HealthCheckResult h WHERE h.monitoredService.id = :serviceId " +
           "AND h.checkedAt >= :since ORDER BY h.checkedAt ASC")
    List<HealthCheckResult> findRecentByServiceId(@Param("serviceId") Long serviceId,
                                                   @Param("since") Instant since);

    @Query("SELECT COUNT(h) FROM HealthCheckResult h WHERE h.monitoredService.id = :serviceId " +
           "AND h.status = 'UP' AND h.checkedAt >= :since")
    long countUpSince(@Param("serviceId") Long serviceId, @Param("since") Instant since);

    @Query("SELECT COUNT(h) FROM HealthCheckResult h WHERE h.monitoredService.id = :serviceId " +
           "AND h.checkedAt >= :since")
    long countTotalSince(@Param("serviceId") Long serviceId, @Param("since") Instant since);
}
