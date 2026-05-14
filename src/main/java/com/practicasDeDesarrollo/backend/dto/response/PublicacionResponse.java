package com.practicasDeDesarrollo.backend.dto.response;


import java.math.BigDecimal;
import java.util.List;

public record PublicacionResponse(
        Long id,
        String descripcion,
        BigDecimal precio,
        List<String> imagenes,
        Boolean esFavorito,
        UsuarioResponse inmobiliaria,
        PropiedadResponse propiedad
) {
}
