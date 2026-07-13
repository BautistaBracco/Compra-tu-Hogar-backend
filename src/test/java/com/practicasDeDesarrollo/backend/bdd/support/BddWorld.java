package com.practicasDeDesarrollo.backend.bdd.support;

import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class BddWorld {

    public final AuthContext auth = new AuthContext();
    public ResponseEntity<String> lastResponse;

    public long extractId() {
        ResponseEntity<String> response = lastResponse;
        if (response == null || response.getBody() == null) {
            throw new IllegalStateException("No hay lastResponse con body para extraer id");
        }
        try {
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            long id = root.path("id").asLong(-1);
            if (id <= 0) {
                throw new IllegalStateException("La respuesta no contiene un id > 0");
            }
            return id;
        } catch (Exception e) {
            throw new AssertionError("No se pudo parsear la respuesta JSON para extraer id", e);
        }
    }

    public static class AuthContext {
        public String jwt;
    }
}
