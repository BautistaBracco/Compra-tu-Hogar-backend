package com.practicasDeDesarrollo.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePublicacionRequest(
        @NotBlank String descripcion,
        @NotNull @DecimalMin("0.01") BigDecimal precio,
        List<String> imagenes
) {}