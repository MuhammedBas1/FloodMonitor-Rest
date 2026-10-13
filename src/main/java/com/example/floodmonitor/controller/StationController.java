package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationList;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.StationService;
import com.example.floodmonitor.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stations")
@Tag(name = "Stations", description = "Abruf und Filterung der Messstationen")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Alle Messstationen abrufen",
            description = "Optionale Filter: river, isActive, warningLevel (aktuellste Messung).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste der Stationen (JSON oder XML)"),
                    @ApiResponse(responseCode = "400", description = "Ungültiger Filterparameter"),
                    @ApiResponse(responseCode = "406", description = "Accept-Header nicht unterstützt")
            })
    public StationList getStations(
            @Parameter(description = "Filter nach Fluss, z. B. Donau")
            @RequestParam(required = false) String river,
            @Parameter(description = "Filter nach Aktivstatus (true/false)")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Filter nach aktueller Warnstufe (NORMAL, WARNING, CRITICAL, UNKNOWN)")
            @RequestParam(required = false) String warningLevel) {

        WarningLevel level = RequestUtils.parseWarningLevel(warningLevel);
        return new StationList(stationService.getStations(river, isActive, level));
    }

    @GetMapping(value = "/{stationId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Einzelne Station abrufen",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Station gefunden"),
                    @ApiResponse(responseCode = "404", description = "Station nicht gefunden"),
                    @ApiResponse(responseCode = "406", description = "Accept-Header nicht unterstützt")
            })
    public Station getStation(@PathVariable String stationId) {
        return stationService.getStationById(stationId);
    }
}