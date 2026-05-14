package com.practicasDeDesarrollo.backend.dto.response;

import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;

import java.util.Set;

public record PropiedadResponse(
        Long id,
        String ubicacion,
        String piso,
        String depto,
        TipoPropiedad tipo,
        Integer superficie,
        Integer ambientes,
        Integer sanitarios,
        Integer expensas,
        Boolean vendida,
        Set<String> caracteristicas,
        Boolean esFavorito
) {
}
