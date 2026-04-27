package com.practicasDeDesarrollo.backend.dto.response;

public record AuthResponse(
        String token,
        Long id,
        String nombre,
        String email,
        String icono,
        String rol
) {
}
