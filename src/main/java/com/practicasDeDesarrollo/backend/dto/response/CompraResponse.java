package com.practicasDeDesarrollo.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraResponse(
        Long idCompra,
        BigDecimal precioFinal,
        LocalDateTime fechaCompra,
        UsuarioResponse comprador,
        PublicacionResponse publicacion

) {
}
