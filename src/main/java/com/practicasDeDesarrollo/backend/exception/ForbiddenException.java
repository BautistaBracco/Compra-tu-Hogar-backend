package com.practicasDeDesarrollo.backend.exception;

/**
 * Accion no permitida para el usuario autenticado (HTTP 403).
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
