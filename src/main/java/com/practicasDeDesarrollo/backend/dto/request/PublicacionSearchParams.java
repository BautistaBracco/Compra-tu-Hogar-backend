package com.practicasDeDesarrollo.backend.dto.request;

import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;

import java.math.BigDecimal;
import java.util.List;

/**
 * Parametros de busqueda para /usuarios/publicaciones.
 *
 * Se usa con @ModelAttribute para agrupar query params y evitar firmas largas.
 */
public record PublicacionSearchParams(
        Boolean vendida,
        TipoPropiedad tipo,
        BigDecimal minPrecio,
        BigDecimal maxPrecio,
        String ubicacion,
        Integer ambientesMin,
        Integer ambientesMax,
        Long inmobiliariaId,
        List<Long> caracteristicaIds
) {
}
