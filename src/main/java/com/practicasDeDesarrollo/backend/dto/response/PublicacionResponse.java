package com.practicasDeDesarrollo.backend.dto.response;


import java.math.BigDecimal;
import java.util.List;

public record PublicacionResponse(
        Long id,
        String descripcion,
        BigDecimal precio,
        Boolean fueVendida,
        List<String> imagenes,
        Long inmobiliariaId,
        Long propiedadId

) {
}