package com.practicasDeDesarrollo.backend.dto.response;

public record TopUserResponse(
        UsuarioResponse usuario,
        Long comprasCount
) {}
