package com.practicasDeDesarrollo.backend.entity.enums;


import org.springframework.security.core.GrantedAuthority;

public enum RolUsuario implements GrantedAuthority {
    COMPRADOR,
    INMOBILIARIA,
    ADMIN;

    @Override
    public String getAuthority() {
        // Al devolver ROLE_ + nombre, habilitás el uso de .hasRole("ADMIN") en la config
        return "ROLE_" + this.name();
    }
}
