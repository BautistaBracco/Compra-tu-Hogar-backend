package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.HealthStatus;
import com.practicasDeDesarrollo.backend.repository.HealthRepository;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final HealthRepository healthRepository;

    public HealthService(HealthRepository healthRepository) {
        this.healthRepository = healthRepository;
    }

    public HealthStatus checkHealth() {
        try {
            boolean dbUp = healthRepository.isDatabaseUp();

            return new HealthStatus(
                    "UP",
                    dbUp ? "UP" : "DOWN",
                    null
            );

        } catch (Exception e) {
            return new HealthStatus(
                    "DOWN",
                    "DOWN",
                    e.getMessage()
            );
        }
    }
}
