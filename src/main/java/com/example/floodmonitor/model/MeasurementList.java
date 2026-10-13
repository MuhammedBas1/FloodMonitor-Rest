package com.example.floodmonitor.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


// Wrapper für Listen, damit XML-Arrays sauber als <measurements><measurement>... geparst werden können
@JacksonXmlRootElement(localName = "measurements")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementList {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "measurement")
    private List<Measurement> measurements;
    // Getter, Setter, Konstruktoren
}