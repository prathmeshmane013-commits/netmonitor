package com.netmonitor.dto;

import com.netmonitor.model.MonitoredService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceRequest {

    @NotBlank
    private String name;

    @NotNull
    private MonitoredService.CheckType checkType;

    @NotBlank
    private String host;

    private Integer port;

    private Integer checkIntervalSeconds = 60;

    private Integer timeoutMs = 5000;

    private Integer expectedStatusCode = 200;

    private String alertEmail;
}
