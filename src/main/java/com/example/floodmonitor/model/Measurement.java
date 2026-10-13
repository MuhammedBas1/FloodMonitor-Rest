package com.example.floodmonitor.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JacksonXmlRootElement(localName = "measurement")
@Data
@NoArgsConstructor
public class Measurement {
    private String id;
    private String stationId;
    private LocalDateTime timestamp;
    private double waterLevel;
    private double flowRate;
    private double rainfall;
    private double temperature;
    private int batteryLevel;
    private StationStatus status;
    private WarningLevel warningLevel;

}