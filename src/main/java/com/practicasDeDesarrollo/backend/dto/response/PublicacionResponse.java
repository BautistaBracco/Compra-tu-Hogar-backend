package com.practicasDeDesarrollo.backend.dto.response;


import java.math.BigDecimal;
import java.util.List;

public record PublicacionResponse(
        Long id,
        String descripcion,
        BigDecimal precio,
        List<String> imagenes,
        UsuarioResponse inmobiliaria,
        PropiedadResponse propiedad,
        UsuarioMetadataResponse metadata
) {
}