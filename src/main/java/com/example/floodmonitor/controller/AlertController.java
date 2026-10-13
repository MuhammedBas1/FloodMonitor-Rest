package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.MeasurementList;
import com.example.floodmonitor.service.MeasurementService;
import com.example.floodmonitor.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Aktuelle Warnmeldungen")
public class AlertController {

    private final MeasurementService measurementService;

    public AlertController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Aktuelle Warnungen abrufen",
            description = "Ohne Filter: alle Messungen mit WARNING oder CRITICAL. Optionale Filter: warningLevel, river.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste der Warnungen"),
                    @ApiResponse(responseCode = "400", description = "Ungültige Warnstufe"),
                    @ApiResponse(responseCode = "406", description = "Accept-Header nicht unterstützt")
            })
    public MeasurementList getAlerts(
            @Parameter(description = "Filter nach Warnstufe")
            @RequestParam(required = false) String warningLevel,
            @Parameter(description = "Filter nach Fluss")
            @RequestParam(required = false) String river) {

        return new MeasurementList(
                measurementService.getAlerts(RequestUtils.parseWarningLevel(warningLevel), river));
    }
}