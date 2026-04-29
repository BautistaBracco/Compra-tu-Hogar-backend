package com.practicasDeDesarrollo.backend.dto.response;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String icono

) {
}
