package com.practicasDeDesarrollo.backend.exception;

/**
 * Conflicto de negocio (HTTP 409). Usar cuando el recurso existe pero
 * el request intenta crear/relacionar con datos incompatibles.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
