package com.netmonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UptimeSummary {
    private Long serviceId;
    private double uptimePercentage; // over the requested window
    private long totalChecks;
    private long upChecks;
}
