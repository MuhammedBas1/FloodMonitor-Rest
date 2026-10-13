package com.example.floodmonitor.service;

import com.example.floodmonitor.exception.ResourceNotFoundException;
import com.example.floodmonitor.model.*;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;

    public StationService(StationRepository stationRepository, MeasurementRepository measurementRepository) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
    }

    /**
     * Alle Stationen, optional gefiltert nach river, isActive, warningLevel.
     */
    public List<Station> getStations(String river, Boolean isActive, WarningLevel warningLevel) {
        List<Station> result = stationRepository.findAll();

        if (river != null && !river.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getRiver().equalsIgnoreCase(river))
                    .collect(Collectors.toList());
        }

        if (isActive != null) {
            result = result.stream()
                    .filter(s -> s.isActive() == isActive)
                    .collect(Collectors.toList());
        }

        if (warningLevel != null) {
            // Nur Stationen, deren aktuellste Messung die gewünschte Warnstufe hat
            result = result.stream()
                    .filter(s -> {
                        Optional<Measurement> latest = measurementRepository.findLatestByStationId(s.getId());
                        return latest.isPresent() && latest.get().getWarningLevel() == warningLevel;
                    })
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Eine einzelne Station by ID.
     */
    public Station getStationById(String stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Station " + stationId + " wurde nicht gefunden"));
    }

    /**
     * Prüft ob eine Station existiert.
     */
    public boolean exists(String stationId) {
        return stationRepository.findById(stationId).isPresent();
    }
}