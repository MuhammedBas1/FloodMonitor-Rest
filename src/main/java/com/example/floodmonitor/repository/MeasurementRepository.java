package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class MeasurementRepository {

    // stationId -> Liste von Measurements (zeitlich sortiert)
    private final Map<String, List<Measurement>> measurements = new ConcurrentHashMap<>();

    public void save(Measurement measurement) {
        measurements.computeIfAbsent(measurement.getStationId(), k -> new CopyOnWriteArrayList<>())
                .add(measurement);
    }

    public List<Measurement> findByStationId(String stationId) {
        return measurements.getOrDefault(stationId, new ArrayList<>());
    }

    public Optional<Measurement> findLatestByStationId(String stationId) {
        List<Measurement> list = measurements.get(stationId);
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(list.size() - 1));
    }

    public List<Measurement> findByStationIdFiltered(String stationId,
                                                     LocalDateTime from,
                                                     LocalDateTime to,
                                                     Integer limit,
                                                     WarningLevel warningLevel) {
        List<Measurement> result = measurements.getOrDefault(stationId, new ArrayList<>())
                .stream()
                .filter(m -> from == null || !m.getTimestamp().isBefore(from))
                .filter(m -> to == null || !m.getTimestamp().isAfter(to))
                .filter(m -> warningLevel == null || m.getWarningLevel() == warningLevel)
                .collect(Collectors.toList());

        // Neueste zuerst
        result.sort(Comparator.comparing(Measurement::getTimestamp).reversed());

        if (limit != null && limit > 0 && result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }

    public List<Measurement> findAll() {
        return measurements.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Measurement> findByWarningLevel(WarningLevel warningLevel) {
        return measurements.values().stream()
                .flatMap(List::stream)
                .filter(m -> m.getWarningLevel() == warningLevel)
                .collect(Collectors.toList());
    }

    public void clear() {
        measurements.clear();
    }
}