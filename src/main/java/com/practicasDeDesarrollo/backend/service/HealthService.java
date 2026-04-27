package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.response.HealthStatusResponse;
import com.practicasDeDesarrollo.backend.repository.HealthRepository;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final HealthRepository healthRepository;

    public HealthService(HealthRepository healthRepository) {
        this.healthRepository = healthRepository;
    }

    public HealthStatusResponse checkHealth() {
        try {
            boolean dbUp = healthRepository.isDatabaseUp();

            return new HealthStatusResponse(
                    "UP",
                    dbUp ? "UP" : "DOWN",
                    null
            );

        } catch (Exception e) {
            return new HealthStatusResponse(
                    "DOWN",
                    "DOWN",
                    e.getMessage()
            );
        }
    }
}
