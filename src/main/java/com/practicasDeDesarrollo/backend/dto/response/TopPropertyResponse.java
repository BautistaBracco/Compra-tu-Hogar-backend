package com.practicasDeDesarrollo.backend.dto.response;

import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;

public record TopPropertyResponse(
        PropiedadResponse propiedad,
        Double averageRating
) {}
