package com.practicasDeDesarrollo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUsuarioRequest(
        String nombre,

        String icono
) {
}
