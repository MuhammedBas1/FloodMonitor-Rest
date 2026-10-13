package com.example.floodmonitor.service;

import com.example.floodmonitor.model.*;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationService {

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final Random random = new Random();

    @Value("${simulation.interval:10000}")
    private long interval;

    public SimulationService(StationRepository stationRepository, MeasurementRepository measurementRepository) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
    }

    @Scheduled(fixedDelayString = "${simulation.interval:10000}")
    public void simulate() {
        for (Station station : stationRepository.findAll()) {
            if (!station.isActive()) continue;

            // Optional korrekt auflösen:
            Measurement last = measurementRepository    .findLatestByStationId(station.getId()).orElse(null);

            measurementRepository.save(generateMeasurement(station, last));
        }
    }

    private Measurement generateMeasurement(Station station, Measurement last) {
        Measurement m = new Measurement();
        m.setId(UUID.randomUUID().toString());
        m.setStationId(station.getId());
        m.setTimestamp(LocalDateTime.now());

        // --- Niederschlag (0 - 20 mm/h) ---
        double rainfall;
        if (last != null) {
            // Langsame Änderung zum vorherigen Wert
            rainfall = Math.max(0, last.getRainfall() + random.nextGaussian() * 2.0);
        } else {
            rainfall = Math.max(0, random.nextDouble() * 10.0);
        }
        rainfall = Math.min(rainfall, 50.0); // Max 50 mm/h
        m.setRainfall(Math.round(rainfall * 10.0) / 10.0);

        // --- Wasserstand (nicht negativ, basiert auf vorherigem Wert) ---
        double lastWaterLevel = (last != null) ? last.getWaterLevel() : station.getNormalWaterLevel();
        // Starker Regen erhöht den Wasserstand tendenziell
        double rainInfluence = m.getRainfall() * 0.5;
        double randomDrift = random.nextGaussian() * 2.0;
        double waterLevel = lastWaterLevel + rainInfluence + randomDrift;
        waterLevel = Math.max(0, waterLevel); // Wasserstand darf nicht negativ sein
        m.setWaterLevel(Math.round(waterLevel * 100.0) / 100.0);

        // --- Durchflussmenge (korreliert mit Wasserstand) ---
        double flowRate = m.getWaterLevel() * (2.0 + random.nextDouble() * 1.5);
        m.setFlowRate(Math.round(flowRate * 10.0) / 10.0);

        // --- Temperatur ---
        double lastTemp = (last != null) ? last.getTemperature() : 15.0;
        double temperature = lastTemp + random.nextGaussian() * 0.3;
        temperature = Math.max(-20.0, Math.min(45.0, temperature));
        m.setTemperature(Math.round(temperature * 10.0) / 10.0);

        // --- Akku ---
        int lastBattery = (last != null) ? last.getBatteryLevel() : 100;
        int batteryDrain = random.nextInt(3); // 0-2% Abnahme
        int batteryLevel = Math.max(0, Math.min(100, lastBattery - batteryDrain));
        m.setBatteryLevel(batteryLevel);

        // --- Status ---
        if (batteryLevel < 10) {
            m.setStatus(StationStatus.MAINTENANCE);
        } else {
            m.setStatus(StationStatus.ONLINE);
        }

        // --- Warnstufe berechnen (serverseitig!) ---
        m.setWarningLevel(calculateWarningLevel(station, m.getWaterLevel()));

        return m;
    }

    /**
     * Warnstufenberechnung gemäß Angabe:
     * - Wasserstand < warningWaterLevel → NORMAL
     * - Wasserstand >= warningWaterLevel → WARNING
     * - Wasserstand >= criticalWaterLevel → CRITICAL
     * - Keine gültige Messung → UNKNOWN
     */
    public WarningLevel calculateWarningLevel(Station station, double waterLevel) {
        if (waterLevel >= station.getCriticalWaterLevel()) {
            return WarningLevel.CRITICAL;
        }
        if (waterLevel >= station.getWarningWaterLevel()) {
            return WarningLevel.WARNING;
        }
        return WarningLevel.NORMAL;
    }
}