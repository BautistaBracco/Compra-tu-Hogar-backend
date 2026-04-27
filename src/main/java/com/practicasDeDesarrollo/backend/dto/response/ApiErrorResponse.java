package com.practicasDeDesarrollo.backend.dto.response;

import java.util.Map;

public record ApiErrorResponse(
        String message,
        Map<String, String> details
) {
}
