package com.example.floodmonitor.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "statistics")
@Data
@NoArgsConstructor
public class Statistics {

    private String stationId;          // null = global
    private double minWaterLevel;
    private double maxWaterLevel;
    private double avgWaterLevel;
    private double avgFlowRate;
    private double totalRainfall;
    private long measurementCount;
    private long warningCount;         // WARNING + CRITICAL
    private long criticalCount;        // nur CRITICAL

}