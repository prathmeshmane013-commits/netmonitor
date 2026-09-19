package com.netmonitor.scheduler;

import com.netmonitor.model.MonitoredService;
import com.netmonitor.repository.MonitoredServiceRepository;
import com.netmonitor.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Ticks every 10 seconds and runs a check for any active service whose
 * per-service interval has elapsed. This lets each monitored service have
 * its own check frequency without needing one scheduled job per service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringScheduler {

    private final MonitoredServiceRepository serviceRepository;
    private final HealthCheckService healthCheckService;

    @Scheduled(fixedRate = 10_000)
    public void runDueChecks() {
        Instant now = Instant.now();
        for (MonitoredService service : serviceRepository.findByActiveTrue()) {
            if (isDue(service, now)) {
                try {
                    healthCheckService.performCheck(service);
                } catch (Exception e) {
                    log.error("Unexpected error checking service {}: {}", service.getName(), e.getMessage());
                }
            }
        }
    }

    private boolean isDue(MonitoredService service, Instant now) {
        if (service.getLastCheckedAt() == null) return true;
        Instant nextDue = service.getLastCheckedAt().plus(service.getCheckIntervalSeconds(), ChronoUnit.SECONDS);
        return !now.isBefore(nextDue);
    }
}
