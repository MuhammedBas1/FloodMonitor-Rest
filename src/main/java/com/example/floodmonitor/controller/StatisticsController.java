package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Statistics;
import com.example.floodmonitor.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Statistics", description = "Statistische Auswertungen")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping(value = "/api/v1/statistics", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Globale Statistik über alle Messungen",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statistik (JSON oder XML)"),
                    @ApiResponse(responseCode = "406", description = "Accept-Header nicht unterstützt")
            })
    public Statistics getGlobalStatistics() {
        return statisticsService.computeGlobal();
    }

    @GetMapping(value = "/api/v1/stations/{stationId}/statistics", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Statistik einer einzelnen Station",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statistik der Station"),
                    @ApiResponse(responseCode = "404", description = "Station nicht gefunden"),
                    @ApiResponse(responseCode = "406", description = "Accept-Header nicht unterstützt")
            })
    public Statistics getStationStatistics(@PathVariable String stationId) {
        return statisticsService.computeForStation(stationId);
    }
}