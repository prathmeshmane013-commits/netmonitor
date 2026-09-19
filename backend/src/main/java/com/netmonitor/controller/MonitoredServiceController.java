package com.netmonitor.controller;

import com.netmonitor.dto.ServiceRequest;
import com.netmonitor.dto.UptimeSummary;
import com.netmonitor.model.HealthCheckResult;
import com.netmonitor.model.MonitoredService;
import com.netmonitor.repository.HealthCheckResultRepository;
import com.netmonitor.repository.MonitoredServiceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class MonitoredServiceController {

    private final MonitoredServiceRepository serviceRepository;
    private final HealthCheckResultRepository resultRepository;

    @GetMapping
    public List<MonitoredService> getAll() {
        return serviceRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonitoredService> getOne(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MonitoredService> create(@Valid @RequestBody ServiceRequest req) {
        MonitoredService service = new MonitoredService();
        applyRequest(service, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRepository.save(service));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonitoredService> update(@PathVariable Long id, @Valid @RequestBody ServiceRequest req) {
        return serviceRepository.findById(id)
                .map(service -> {
                    applyRequest(service, req);
                    return ResponseEntity.ok(serviceRepository.save(service));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!serviceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public List<HealthCheckResult> getHistory(@PathVariable Long id,
                                               @RequestParam(defaultValue = "24") int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return resultRepository.findRecentByServiceId(id, since);
    }

    @GetMapping("/{id}/uptime")
    public UptimeSummary getUptime(@PathVariable Long id,
                                    @RequestParam(defaultValue = "24") int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        long total = resultRepository.countTotalSince(id, since);
        long up = resultRepository.countUpSince(id, since);
        double pct = total == 0 ? 0.0 : (up * 100.0) / total;
        return new UptimeSummary(id, Math.round(pct * 100.0) / 100.0, total, up);
    }

    private void applyRequest(MonitoredService service, ServiceRequest req) {
        service.setName(req.getName());
        service.setCheckType(req.getCheckType());
        service.setHost(req.getHost());
        service.setPort(req.getPort());
        service.setCheckIntervalSeconds(req.getCheckIntervalSeconds());
        service.setTimeoutMs(req.getTimeoutMs());
        service.setExpectedStatusCode(req.getExpectedStatusCode());
        service.setAlertEmail(req.getAlertEmail());
    }
}
