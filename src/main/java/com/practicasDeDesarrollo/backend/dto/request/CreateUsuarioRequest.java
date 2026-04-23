package com.practicasDeDesarrollo.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUsuarioRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        String icon
) {
}
