package com.netmonitor.service;

import com.netmonitor.model.MonitoredService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final JavaMailSender mailSender;

    public void sendStatusChangeAlert(MonitoredService service,
                                       MonitoredService.ServiceStatus oldStatus,
                                       MonitoredService.ServiceStatus newStatus) {
        if (service.getAlertEmail() == null || service.getAlertEmail().isBlank()) {
            log.info("Service {} changed {} -> {} (no alert email configured)",
                    service.getName(), oldStatus, newStatus);
            return;
        }

        String subject = String.format("[NetMonitor] %s is now %s", service.getName(), newStatus);
        String body = String.format(
                "Service: %s%nHost: %s%nPrevious status: %s%nNew status: %s%nChecked at: %s",
                service.getName(), service.getHost(), oldStatus, newStatus, service.getLastCheckedAt());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(service.getAlertEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Don't let a mail server outage break the monitoring loop itself.
            log.error("Failed to send alert email for service {}: {}", service.getName(), e.getMessage());
        }
    }
}
