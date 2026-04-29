package com.practicasDeDesarrollo.backend.dto.request;

import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

public record CreatePropiedadRequest(
        @NotNull TipoPropiedad tipo,
        @NotBlank String ubicacion,
        String piso,                // Opcional: "4", "PB"
        String depto,               // Opcional: "A", "1"
        @Positive Integer superficie,
        @Positive Integer ambientes,
        @PositiveOrZero Integer sanitarios,
        @PositiveOrZero Integer expensas
) {}