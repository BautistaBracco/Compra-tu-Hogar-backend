package com.practicasDeDesarrollo.backend.dto.response;

public record ResenaResponse(
        Long id,
        Integer puntaje,
        String comentario,
        Long autorId,
        String autorNombre,
        Long publicacionId
) {
}
