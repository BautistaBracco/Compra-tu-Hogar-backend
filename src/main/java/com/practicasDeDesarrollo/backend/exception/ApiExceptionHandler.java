package com.practicasDeDesarrollo.backend.exception;

import com.practicasDeDesarrollo.backend.dto.response.ApiError;
import com.practicasDeDesarrollo.backend.dto.response.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private ApiError buildError(
            HttpStatus status,
            ErrorCode code,
            String message,
            String path,
            Map<String, Object> details
    ) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code.name(),
                message,
                path,
                details
        );
    }

    private static String requestPath(HttpServletRequest request) {
        // Keep it aligned with Security handlers and typical Spring behavior
        return request.getRequestURI();
    }

    // 🔹 1. Validaciones (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
                details.put(err.getField(), err.getDefaultMessage())
        );

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Error en los datos enviados",
                requestPath(request),
                details
        );

        return ResponseEntity.badRequest().body(error);
    }

    // 🔹 2. JSON inválido / ENUM mal
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Throwable cause = ex.getMostSpecificCause();

        // Caso enum inválido
        if (cause instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {

            String campo = ife.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .collect(Collectors.joining("."));

            Map<String, Object> details = new HashMap<>();
            details.put("campo", campo);
            details.put("valor", ife.getValue());
            details.put("permitidos", ife.getTargetType().getEnumConstants());

            ApiError error = buildError(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INVALID_ENUM,
                    "Valor inválido para enum",
                    requestPath(request),
                    details
            );

            return ResponseEntity.badRequest().body(error);
        }

        // JSON mal formado genérico
        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.JSON_MALFORMED,
                "JSON mal formado",
                requestPath(request),
                null
        );

        return ResponseEntity.badRequest().body(error);
    }

    // 🔹 3. Errores de negocio
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBusiness(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BUSINESS_ERROR,
                ex.getMessage(),
                requestPath(request),
                null
        );

        return ResponseEntity.badRequest().body(error);
    }

    // 🔹 4. Recurso no encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                ex.getMessage(),
                requestPath(request),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 🔹 5. Conflictos de integridad (duplicados, unique constraints, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                "Conflicto: el recurso ya existe o viola una restriccion de integridad",
                requestPath(request),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 🔹 5b. Conflictos de negocio (datos incompatibles)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleBusinessConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                ex.getMessage(),
                requestPath(request),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 🔹 5c. Autenticación (credenciales inválidas)
    @ExceptionHandler({AuthenticationException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleAuth(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        // Mensaje genérico para no filtrar si el usuario existe
        ApiError error = buildError(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.AUTH_INVALID_CREDENTIALS,
                "Credenciales inválidas",
                requestPath(request),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 🔹 6. Fallback (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobal(
            Exception ex,
            HttpServletRequest request
    ) {
        ex.printStackTrace(); // trace completo en consola
        ApiError error = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "Error interno del servidor",
                requestPath(request),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
