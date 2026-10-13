package com.example.floodmonitor;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import com.example.floodmonitor.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FloodMonitorTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    @BeforeEach
    void setUp() {
        measurementRepository.clear();
        // Deterministische Testdaten (Simulation ist in Tests deaktiviert)
        measurementRepository.save(measurement("ST-001", LocalDateTime.of(2026, 5, 1, 8, 0), 100.0, WarningLevel.NORMAL));
        measurementRepository.save(measurement("ST-001", LocalDateTime.of(2026, 5, 1, 10, 0), 350.0, WarningLevel.WARNING));
        measurementRepository.save(measurement("ST-001", LocalDateTime.of(2026, 5, 1, 12, 0), 450.0, WarningLevel.CRITICAL));
    }

    private Measurement measurement(String stationId, LocalDateTime time, double waterLevel, WarningLevel level) {
        Measurement m = new Measurement();
        m.setId(UUID.randomUUID().toString());
        m.setStationId(stationId);
        m.setTimestamp(time);
        m.setWaterLevel(waterLevel);
        m.setFlowRate(waterLevel * 2);
        m.setRainfall(0);
        m.setTemperature(15);
        m.setBatteryLevel(100);
        m.setStatus(StationStatus.ONLINE);
        m.setWarningLevel(level);
        return m;
    }

    @Test
    void alleStationenAlsJson() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stations").isArray());
    }

    @Test
    void alleStationenAlsXml() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/stations/station").exists());
    }

    @Test
    void existierendeStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ST-001"));
    }

    @Test
    void unbekannteStationLiefert404() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Station ST-999 wurde nicht gefunden"));
    }

    @Test
    void aktuellsterMesswert() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements/latest").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId").value("ST-001"))
                .andExpect(jsonPath("$.warningLevel").value("CRITICAL"));
    }

    @Test
    void filterungNachZeitraum() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2026-05-01T09:00:00")
                        .param("to", "2026-05-01T11:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].warningLevel").value("WARNING"));
    }

    @Test
    void ungueltigerZeitraumFromNachToLiefert400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2026-05-01T12:00:00")
                        .param("to", "2026-05-01T08:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void ungueltigesDatumsformatLiefert400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "01.05.2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void warnstufeNormal() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("warningLevel", "NORMAL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].warningLevel").value("NORMAL"));
    }

    @Test
    void warnstufeWarning() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("warningLevel", "WARNING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].warningLevel").value("WARNING"));
    }

    @Test
    void warnstufeCritical() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("warningLevel", "CRITICAL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].warningLevel").value("CRITICAL"));
    }

    @Test
    void warnstufenBerechnungServerseitig() {
        // SimulationService manuell instanziieren (Bean ist in Tests deaktiviert)
        SimulationService simulation = new SimulationService(stationRepository, measurementRepository);
        Station station = stationRepository.findById("ST-001").orElseThrow();

        assertEquals(WarningLevel.NORMAL, simulation.calculateWarningLevel(station, 100.0));
        assertEquals(WarningLevel.WARNING, simulation.calculateWarningLevel(station, station.getWarningWaterLevel()));
        assertEquals(WarningLevel.CRITICAL, simulation.calculateWarningLevel(station, station.getCriticalWaterLevel()));
    }

    @Test
    void nichtUnterstuetztesFormatLiefert406() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept("application/pdf"))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.status").value(406));
    }

    @Test
    void maximalesLimitErlaubt() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("limit", "1000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void limitUeberschrittenLiefert400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("limit", "1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativerLimitLiefert400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("limit", "-5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}