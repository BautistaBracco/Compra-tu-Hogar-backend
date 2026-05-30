package com.practicasDeDesarrollo.backend.bdd.support;

import org.springframework.http.ResponseEntity;

public class BddWorld {
    public ResponseEntity<String> lastResponse;
    public String jwt;

    // Helps keep steps stateless and avoids hardcoding in scenarios.
    public String lastEmail;
}
