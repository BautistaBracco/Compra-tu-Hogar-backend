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

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String EXCEPTION_TYPE = "exceptionType";
    private static final String EXCEPTION_MESSAGE = "exceptionMessage";

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

        MDC.put(EXCEPTION_TYPE, "MethodArgumentNotValidException");
        MDC.put(EXCEPTION_MESSAGE, "Error en los datos enviados");
        log.warn("Validation error: {} - {}", requestPath(request), details);

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

            MDC.put(EXCEPTION_TYPE, "InvalidFormatException");
            MDC.put(EXCEPTION_MESSAGE, "Valor inválido para enum: " + ife.getValue());
            log.warn("Invalid enum value for field {}: {} - {}", campo, ife.getValue(), requestPath(request));

            ApiError error = buildError(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INVALID_ENUM,
                    "Valor inválido para enum",
                    requestPath(request),
                    details
            );

            return ResponseEntity.badRequest().body(error);
        }

        MDC.put(EXCEPTION_TYPE, "HttpMessageNotReadableException");
        MDC.put(EXCEPTION_MESSAGE, "JSON mal formado");
        log.warn("Malformed JSON: {} - {}", requestPath(request), ex.getMessage());

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
        MDC.put(EXCEPTION_TYPE, "IllegalArgumentException");
        MDC.put(EXCEPTION_MESSAGE, ex.getMessage());
        log.warn("Business error: {} - {}", ex.getMessage(), requestPath(request));

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
        MDC.put(EXCEPTION_TYPE, "EntityNotFoundException");
        MDC.put(EXCEPTION_MESSAGE, ex.getMessage());
        log.warn("Resource not found: {} - {}", ex.getMessage(), requestPath(request));

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
        MDC.put(EXCEPTION_TYPE, "DataIntegrityViolationException");
        MDC.put(EXCEPTION_MESSAGE, ex.getMessage());
        log.warn("Data integrity violation: {} - {}", requestPath(request), ex.getMessage());

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
        MDC.put(EXCEPTION_TYPE, "ConflictException");
        MDC.put(EXCEPTION_MESSAGE, ex.getMessage());
        log.warn("Business conflict: {} - {}", ex.getMessage(), requestPath(request));

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
        MDC.put(EXCEPTION_TYPE, ex.getClass().getSimpleName());
        MDC.put(EXCEPTION_MESSAGE, "Credenciales inválidas");
        log.warn("Authentication failed: {} - {}", ex.getClass().getSimpleName(), requestPath(request));

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

    // 🔹 5d. Autorización (no tiene permisos)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        MDC.put(EXCEPTION_TYPE, "ForbiddenException");
        MDC.put(EXCEPTION_MESSAGE, ex.getMessage());
        log.warn("Forbidden: {} - {}", ex.getMessage(), requestPath(request));

        ApiError error = buildError(
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN,
                ex.getMessage(),
                requestPath(request),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 🔹 6. Fallback (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobal(
            Exception ex,
            HttpServletRequest request
    ) {
        MDC.put(EXCEPTION_TYPE, "Exception");
        MDC.put(EXCEPTION_MESSAGE, ex.getMessage());
        log.error("Unhandled exception processing {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
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
