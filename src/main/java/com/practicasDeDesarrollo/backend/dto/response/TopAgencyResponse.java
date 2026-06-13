package com.practicasDeDesarrollo.backend.dto.response;

public record TopAgencyResponse(
        UsuarioResponse inmobiliaria,
        Long ventasCount
) {}
