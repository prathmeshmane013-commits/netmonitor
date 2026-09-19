package com.netmonitor.service;

import com.netmonitor.model.HealthCheckResult;
import com.netmonitor.model.MonitoredService;
import com.netmonitor.repository.HealthCheckResultRepository;
import com.netmonitor.repository.MonitoredServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.time.Instant;

/**
 * Performs the actual network checks for a monitored service.
 * Supports three check types:
 *  - HTTP: opens a connection and checks the response status code
 *  - TCP_PORT: attempts a raw socket connection to host:port
 *  - PING: uses Java's InetAddress.isReachable (ICMP or TCP echo fallback)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final HealthCheckResultRepository resultRepository;
    private final MonitoredServiceRepository serviceRepository;
    private final AlertService alertService;

    public void performCheck(MonitoredService service) {
        long start = System.currentTimeMillis();
        HealthCheckResult result = new HealthCheckResult();
        result.setMonitoredService(service);
        result.setCheckedAt(Instant.now());

        try {
            switch (service.getCheckType()) {
                case HTTP -> checkHttp(service, result);
                case TCP_PORT -> checkTcpPort(service, result);
                case PING -> checkPing(service, result);
            }
        } catch (Exception e) {
            result.setStatus(MonitoredService.ServiceStatus.DOWN);
            result.setErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
            log.warn("Health check failed for service {}: {}", service.getName(), e.getMessage());
        }

        result.setResponseTimeMs(System.currentTimeMillis() - start);
        resultRepository.save(result);

        updateServiceStatus(service, result.getStatus());
    }

    private void checkHttp(MonitoredService service, HealthCheckResult result) throws IOException {
        URL url = new URL(service.getHost());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(service.getTimeoutMs());
        connection.setReadTimeout(service.getTimeoutMs());

        int statusCode = connection.getResponseCode();
        result.setStatusCode(statusCode);

        boolean healthy = statusCode == service.getExpectedStatusCode()
                || (statusCode >= 200 && statusCode < 400);
        result.setStatus(healthy ? MonitoredService.ServiceStatus.UP : MonitoredService.ServiceStatus.DOWN);
        connection.disconnect();
    }

    private void checkTcpPort(MonitoredService service, HealthCheckResult result) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(service.getHost(), service.getPort()), service.getTimeoutMs());
            result.setStatus(MonitoredService.ServiceStatus.UP);
        }
    }

    private void checkPing(MonitoredService service, HealthCheckResult result) throws IOException {
        boolean reachable = java.net.InetAddress.getByName(service.getHost())
                .isReachable(service.getTimeoutMs());
        result.setStatus(reachable ? MonitoredService.ServiceStatus.UP : MonitoredService.ServiceStatus.DOWN);
    }

    private void updateServiceStatus(MonitoredService service, MonitoredService.ServiceStatus newStatus) {
        MonitoredService.ServiceStatus oldStatus = service.getCurrentStatus();
        service.setCurrentStatus(newStatus);
        service.setLastCheckedAt(Instant.now());

        if (oldStatus != newStatus) {
            service.setLastStatusChangeAt(Instant.now());
            // Only alert on transitions we actually care about, and not on the
            // very first check (UNKNOWN -> anything) to avoid noise at startup.
            if (oldStatus != MonitoredService.ServiceStatus.UNKNOWN) {
                alertService.sendStatusChangeAlert(service, oldStatus, newStatus);
            }
        }
        serviceRepository.save(service);
    }
}
