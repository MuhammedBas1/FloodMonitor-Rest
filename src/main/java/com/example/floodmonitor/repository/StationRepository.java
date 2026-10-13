package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.Location;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class StationRepository {

    private final Map<String, Station> stations = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Mindestens 3 Messstationen gemäß Angabe
        stations.put("ST-001", new Station(
                "ST-001", "Station Vienna", "Donau",
                new Location(48.2082, 16.3738),
                200.0, 300.0, 400.0, true));

        stations.put("ST-002", new Station(
                "ST-002", "Station Linz", "Donau",
                new Location(48.3069, 14.2858),
                180.0, 280.0, 380.0, true));

        stations.put("ST-003", new Station(
                "ST-003", "Station Salzburg", "Salzach",
                new Location(47.8095, 13.0550),
                150.0, 250.0, 350.0, true));

        stations.put("ST-004", new Station(
                "ST-004", "Station Graz", "Mur",
                new Location(47.0707, 15.4395),
                160.0, 260.0, 360.0, true));

        stations.put("ST-005", new Station(
                "ST-005", "Station Innsbruck", "Inn",
                new Location(47.2692, 11.4041),
                140.0, 240.0, 340.0, false)); // inaktiv
    }

    public List<Station> findAll() {
        return new ArrayList<>(stations.values());
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    public List<Station> findByRiver(String river) {
        return stations.values().stream()
                .filter(s -> s.getRiver().equalsIgnoreCase(river))
                .collect(Collectors.toList());
    }

    public List<Station> findByIsActive(boolean isActive) {
        return stations.values().stream()
                .filter(s -> s.isActive() == isActive)
                .collect(Collectors.toList());
    }

    public void save(Station station) {
        stations.put(station.getId(), station);
    }
}