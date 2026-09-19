package com.netmonitor.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "monitored_services")
@Data
public class MonitoredService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckType checkType; // HTTP, TCP_PORT, PING

    @Column(nullable = false)
    private String host; // hostname or URL depending on checkType

    private Integer port; // used for TCP_PORT checks

    @Column(name = "check_interval_seconds")
    private Integer checkIntervalSeconds = 60;

    @Column(name = "timeout_ms")
    private Integer timeoutMs = 5000;

    @Column(name = "expected_status_code")
    private Integer expectedStatusCode = 200; // used for HTTP checks

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus currentStatus = ServiceStatus.UNKNOWN;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Column(name = "last_status_change_at")
    private Instant lastStatusChangeAt;

    @Column(name = "alert_email")
    private String alertEmail;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public enum CheckType {
        HTTP, TCP_PORT, PING
    }

    public enum ServiceStatus {
        UP, DOWN, UNKNOWN
    }
}
