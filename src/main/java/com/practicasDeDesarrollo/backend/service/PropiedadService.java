package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.repository.PropiedadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropiedadService {
    private final PropiedadRepository propiedadRepository;

    @Transactional
    public Propiedad buscarOCrear(@NonNull CreatePropiedadRequest req) {
        // Normalización básica para evitar duplicados por formato
        String ubicacionNorm = req.ubicacion().trim().toUpperCase();
        String pisoNorm = (req.piso() != null) ? req.piso().trim().toUpperCase() : "";
        String deptoNorm = (req.depto() != null) ? req.depto().trim().toUpperCase() : "";

        return propiedadRepository.findByUbicacionAndPisoAndDepto(
                ubicacionNorm, pisoNorm, deptoNorm
        ).orElseGet(() -> {
            Propiedad nueva = Propiedad.builder()
                    .ubicacion(ubicacionNorm)
                    .piso(pisoNorm)
                    .depto(deptoNorm)
                    .tipo(req.tipo())
                    .superficie(req.superficie())
                    .ambientes(req.ambientes())
                    .sanitarios(req.sanitarios())
                    .expensas(req.expensas())
                    .build();
            return propiedadRepository.save(nueva);
        });
    }
}