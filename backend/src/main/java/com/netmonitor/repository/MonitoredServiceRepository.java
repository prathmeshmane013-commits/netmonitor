package com.netmonitor.repository;

import com.netmonitor.model.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, Long> {
    List<MonitoredService> findByActiveTrue();
}
