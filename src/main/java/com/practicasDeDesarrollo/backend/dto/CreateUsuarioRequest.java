package com.practicasDeDesarrollo.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUsuarioRequest(
        @NotBlank String nombre,
        @Email @NotBlank String email,
        @NotBlank String password,
        String icono
) {
}
