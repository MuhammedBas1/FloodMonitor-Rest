package com.example.floodmonitor.service;

import com.example.floodmonitor.exception.ResourceNotFoundException;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Statistics;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class StatisticsService {

    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;

    public StatisticsService(MeasurementRepository measurementRepository, StationRepository stationRepository) {
        this.measurementRepository = measurementRepository;
        this.stationRepository = stationRepository;
    }

    public Statistics computeGlobal() {
        return compute(measurementRepository.findAll(), null);
    }

    public Statistics computeForStation(String stationId) {
        if (stationRepository.findById(stationId).isEmpty()) {
            throw new ResourceNotFoundException("Station " + stationId + " wurde nicht gefunden");
        }
        return compute(measurementRepository.findByStationId(stationId), stationId);
    }

    private Statistics compute(List<Measurement> list, String stationId) {
        Statistics s = new Statistics();
        s.setStationId(stationId);
        s.setMeasurementCount(list.size());

        if (list.isEmpty()) {
            s.setMinWaterLevel(0);
            s.setMaxWaterLevel(0);
            s.setAvgWaterLevel(0);
            s.setAvgFlowRate(0);
            s.setTotalRainfall(0);
        } else {
            DoubleSummaryStatistics water = list.stream()
                    .mapToDouble(Measurement::getWaterLevel).summaryStatistics();
            s.setMinWaterLevel(round2(water.getMin()));
            s.setMaxWaterLevel(round2(water.getMax()));
            s.setAvgWaterLevel(round2(water.getAverage()));
            s.setAvgFlowRate(round2(list.stream().mapToDouble(Measurement::getFlowRate).average().orElse(0)));
            s.setTotalRainfall(round2(list.stream().mapToDouble(Measurement::getRainfall).sum()));
        }

        s.setWarningCount(list.stream()
                .filter(m -> m.getWarningLevel() == WarningLevel.WARNING
                        || m.getWarningLevel() == WarningLevel.CRITICAL).count());
        s.setCriticalCount(list.stream()
                .filter(m -> m.getWarningLevel() == WarningLevel.CRITICAL).count());

        return s;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}