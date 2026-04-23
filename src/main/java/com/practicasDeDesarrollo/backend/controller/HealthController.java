package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.response.HealthStatusResponse;
import com.practicasDeDesarrollo.backend.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<HealthStatusResponse> health() {
        HealthStatusResponse status = healthService.checkHealth();

        if ("UP".equals(status.status())) {
            return ResponseEntity.ok(status);
        }

        return ResponseEntity.status(503).body(status);
    }
}