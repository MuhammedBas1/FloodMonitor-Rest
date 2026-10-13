package com.example.floodmonitor.service;

import com.example.floodmonitor.exception.BadRequestException;
import com.example.floodmonitor.exception.ResourceNotFoundException;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class MeasurementService {

    private static final int MAX_LIMIT = 1000;

    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;

    public MeasurementService(MeasurementRepository measurementRepository, StationRepository stationRepository) {
        this.measurementRepository = measurementRepository;
        this.stationRepository = stationRepository;
    }

    /**
     * Aktuellster Messwert einer Station.
     */
    public Measurement getLatestMeasurement(String stationId) {
        validateStationExists(stationId);
        return measurementRepository.findLatestByStationId(stationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Keine Messung für Station " + stationId + " vorhanden"));
    }

    /**
     * Historische Messwerte mit Filterung.
     */
    public List<Measurement> getMeasurements(String stationId,
                                             String fromStr,
                                             String toStr,
                                             Integer limit,
                                             String warningLevelStr) {
        validateStationExists(stationId);

        LocalDateTime from = parseDateTime(fromStr, "from");
        LocalDateTime to = parseDateTime(toStr, "to");

        // Validierung: from darf nicht nach to liegen
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException(
                    "Ungültiger Zeitraum: 'from' (" + fromStr + ") liegt nach 'to' (" + toStr + ")");
        }

        // Validierung: limit
        if (limit != null) {
            if (limit <= 0) {
                throw new BadRequestException("Parameter 'limit' muss größer als 0 sein.");
            }
            if (limit > MAX_LIMIT) {
                throw new BadRequestException(
                        "Parameter 'limit' darf maximal " + MAX_LIMIT + " betragen. Angegeben: " + limit);
            }
        }

        // Validierung: warningLevel
        WarningLevel warningLevel = parseWarningLevel(warningLevelStr);

        return measurementRepository.findByStationIdFiltered(stationId, from, to, limit, warningLevel);
    }

    /**
     * Alle aktuellen Warnungen (optional gefiltert).
     */
    public List<Measurement> getAlerts(WarningLevel warningLevel, String river) {
        List<Measurement> alerts;

        if (warningLevel != null) {
            alerts = measurementRepository.findByWarningLevel(warningLevel);
        } else {
            // Alle Measurements mit WARNING oder CRITICAL
            alerts = measurementRepository.findAll().stream()
                    .filter(m -> m.getWarningLevel() == WarningLevel.WARNING
                            || m.getWarningLevel() == WarningLevel.CRITICAL)
                    .collect(java.util.stream.Collectors.toList());
        }

        if (river != null && !river.isBlank()) {
            alerts = alerts.stream()
                    .filter(m -> {
                        var station = stationRepository.findById(m.getStationId());
                        return station.isPresent() && station.get().getRiver().equalsIgnoreCase(river);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        return alerts;
    }

    private void validateStationExists(String stationId) {
        if (stationRepository.findById(stationId).isEmpty()) {
            throw new ResourceNotFoundException("Station " + stationId + " wurde nicht gefunden");
        }
    }

    private LocalDateTime parseDateTime(String value, String paramName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(
                    "Ungültiges Datumsformat für Parameter '" + paramName + "'. Erwartet: ISO-8601 (z.B. 2026-05-01T08:00:00)");
        }
    }

    private WarningLevel parseWarningLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return WarningLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Ungültige Warnstufe: '" + value + "'. Erlaubt: NORMAL, WARNING, CRITICAL, UNKNOWN");
        }
    }
}