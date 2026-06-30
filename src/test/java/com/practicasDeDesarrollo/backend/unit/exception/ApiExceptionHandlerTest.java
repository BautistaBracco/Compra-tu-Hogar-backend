package com.practicasDeDesarrollo.backend.unit.exception;

import com.practicasDeDesarrollo.backend.dto.response.ApiError;
import com.practicasDeDesarrollo.backend.dto.response.ErrorCode;
import com.practicasDeDesarrollo.backend.exception.ApiExceptionHandler;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiExceptionHandler — Unit Tests")
class ApiExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient().when(request.getMethod()).thenReturn("GET");
    }

    @Test
    @DisplayName("handleValidation: 400 con detalles de campo")
    void handleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "email", "El email es obligatorio")
        ));

        ResponseEntity<ApiError> res = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ErrorCode.VALIDATION_ERROR.name(), res.getBody().code());
        Map<String, Object> details = res.getBody().details();
        assertEquals("El email es obligatorio", details.get("email"));
    }

    @Test
    @DisplayName("handleJson (enum inválido): 400 con detalles del enum")
    void handleJson_invalidEnum() {
        InvalidFormatException ife = mock(InvalidFormatException.class);
        when(ife.getTargetType()).thenReturn((Class) TestEnum.class);
        when(ife.getValue()).thenReturn("INVALID_VALUE");
        JacksonException.Reference ref = mock(JacksonException.Reference.class);
        when(ref.getPropertyName()).thenReturn("tipo");
        when(ife.getPath()).thenReturn(List.of(ref));
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("msg", ife, mock(HttpInputMessage.class));

        ResponseEntity<ApiError> res = handler.handleJson(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ErrorCode.INVALID_ENUM.name(), res.getBody().code());
    }

    private enum TestEnum { A, B }

    @Test
    @DisplayName("handleJson (genérico): 400 para JSON mal formado")
    void handleJson_malformed() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", mock(HttpInputMessage.class));

        ResponseEntity<ApiError> res = handler.handleJson(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ErrorCode.JSON_MALFORMED.name(), res.getBody().code());
        assertEquals("JSON mal formado", res.getBody().message());
    }

    @Test
    @DisplayName("handleBusiness: 400 con mensaje de error")
    void handleBusiness() {
        IllegalArgumentException ex = new IllegalArgumentException("Valor inválido");

        ResponseEntity<ApiError> res = handler.handleBusiness(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ErrorCode.BUSINESS_ERROR.name(), res.getBody().code());
        assertEquals("Valor inválido", res.getBody().message());
    }

    @Test
    @DisplayName("handleNotFound: 404 con mensaje")
    void handleNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Usuario no encontrado");

        ResponseEntity<ApiError> res = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals(ErrorCode.NOT_FOUND.name(), res.getBody().code());
        assertEquals("Usuario no encontrado", res.getBody().message());
    }

    @Test
    @DisplayName("handleConflict (DataIntegrityViolationException): 409")
    void handleConflict_dataIntegrity() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation");

        ResponseEntity<ApiError> res = handler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertEquals(ErrorCode.CONFLICT.name(), res.getBody().code());
    }

    @Test
    @DisplayName("handleBusinessConflict: 409 con mensaje de negocio")
    void handleBusinessConflict() {
        ConflictException ex = new ConflictException("El email ya está en uso");

        ResponseEntity<ApiError> res = handler.handleBusinessConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertEquals(ErrorCode.CONFLICT.name(), res.getBody().code());
        assertEquals("El email ya está en uso", res.getBody().message());
    }

    @Test
    @DisplayName("handleAuth: 401 genérico")
    void handleAuth() {
        AuthenticationException ex = mock(AuthenticationException.class);

        ResponseEntity<ApiError> res = handler.handleAuth(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals(ErrorCode.AUTH_INVALID_CREDENTIALS.name(), res.getBody().code());
        assertEquals("Credenciales inválidas", res.getBody().message());
    }

    @Test
    @DisplayName("handleForbidden: 403 con mensaje")
    void handleForbidden() {
        ForbiddenException ex = new ForbiddenException("Acceso denegado");

        ResponseEntity<ApiError> res = handler.handleForbidden(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(ErrorCode.FORBIDDEN.name(), res.getBody().code());
        assertEquals("Acceso denegado", res.getBody().message());
    }

    @Test
    @DisplayName("handleGlobal: 500 para excepciones no manejadas")
    void handleGlobal() {
        RuntimeException ex = new RuntimeException("Error inesperado");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/comprador/comprar/1");

        ResponseEntity<ApiError> res = handler.handleGlobal(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), res.getBody().code());
        assertEquals("Error interno del servidor", res.getBody().message());
    }
}
