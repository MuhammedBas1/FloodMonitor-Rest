package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.MeasurementList;
import com.example.floodmonitor.service.MeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stations/{stationId}/measurements")
@Tag(name = "Measurements", description = "Aktuelle und historische Messwerte")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @GetMapping(value = "/latest", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Aktuellsten Messwert einer Station abrufen",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Aktuellste Messung"),
                    @ApiResponse(responseCode = "404", description = "Station nicht gefunden / keine Messung vorhanden"),
                    @ApiResponse(responseCode = "406", description = "Accept-Header nicht unterstützt")
            })
    public Measurement getLatestMeasurement(@PathVariable String stationId) {
        return measurementService.getLatestMeasurement(stationId);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Historische Messwerte abrufen",
            description = "Filter: from, to (ISO-8601), limit (1-1000), warningLevel.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Gefilterte Messwerte"),
                    @ApiResponse(responseCode = "400", description = "Ungültiger Zeitraum, Limit oder Warnstufe"),
                    @ApiResponse(responseCode = "404", description = "Station nicht gefunden"),
                    @ApiResponse(responseCode = "406", description = "Accept-Header nicht unterstützt")
            })
    public MeasurementList getMeasurements(
            @PathVariable String stationId,
            @Parameter(description = "Startzeitpunkt ISO-8601, z. B. 2026-05-01T08:00:00")
            @RequestParam(required = false) String from,
            @Parameter(description = "Endzeitpunkt ISO-8601")
            @RequestParam(required = false) String to,
            @Parameter(description = "Maximale Anzahl Ergebnisse (1-1000)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Filter nach Warnstufe")
            @RequestParam(required = false) String warningLevel) {

        return new MeasurementList(
                measurementService.getMeasurements(stationId, from, to, limit, warningLevel));
    }
}