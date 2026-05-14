package com.practicasDeDesarrollo.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateResenaRequest(
        @NotNull
        @Min(0)
        @Max(10)
        Integer puntaje,

        String comentario
) {
}
