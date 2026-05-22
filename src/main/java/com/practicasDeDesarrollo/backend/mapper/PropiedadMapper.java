package com.practicasDeDesarrollo.backend.mapper;

import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;
import com.practicasDeDesarrollo.backend.entity.Caracteristica;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PropiedadMapper {

    public PropiedadResponse toResponse(Propiedad propiedad) {
        if (propiedad == null) return null;

        Set<String> nombresCaracteristicas = propiedad.getCaracteristicas()
                .stream()
                .map(Caracteristica::getNombre)
                .collect(Collectors.toSet());

        return new PropiedadResponse(
                propiedad.getId(),
                propiedad.getUbicacion(),
                propiedad.getPiso(),
                propiedad.getDepto(),
                propiedad.getTipo(),
                propiedad.getSuperficie(),
                propiedad.getAmbientes(),
                propiedad.getSanitarios(),
                propiedad.getExpensas(),
                propiedad.getVendida(),
                nombresCaracteristicas
        );
    }
}