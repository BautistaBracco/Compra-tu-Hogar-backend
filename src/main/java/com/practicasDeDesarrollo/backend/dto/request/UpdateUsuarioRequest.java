package com.practicasDeDesarrollo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUsuarioRequest(
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,

        String icono
) {
}
