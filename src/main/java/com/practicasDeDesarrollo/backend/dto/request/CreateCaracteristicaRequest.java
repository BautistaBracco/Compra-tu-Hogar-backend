package com.practicasDeDesarrollo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCaracteristicaRequest(
        @NotBlank String nombre
) {
}
