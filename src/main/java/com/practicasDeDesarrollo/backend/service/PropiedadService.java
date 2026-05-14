package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;
import com.practicasDeDesarrollo.backend.entity.Caracteristica;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.repository.CaracteristicaRepository;
import com.practicasDeDesarrollo.backend.repository.PropiedadRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropiedadService {
    private final PropiedadRepository propiedadRepository;
    private final CaracteristicaRepository caracteristicaRepository;

    private static String normRequired(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normOptional(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean eq(Object a, Object b) {
        return java.util.Objects.equals(a, b);
    }

    @Transactional
    public Propiedad buscarOCrear(@NonNull CreatePropiedadRequest req) {
        String ubicacionNorm = normRequired(req.ubicacion());
        String pisoNorm = normOptional(req.piso());
        String deptoNorm = normOptional(req.depto());

        // 1. Resolvemos características desde el catálogo (si vienen IDs)
        Set<Caracteristica> caracteristicasAsignadas = new HashSet<>();
        if (req.caracteristicaIds() != null && !req.caracteristicaIds().isEmpty()) {
            caracteristicasAsignadas = new HashSet<>(
                    caracteristicaRepository.findAllById(req.caracteristicaIds())
            );

            if (caracteristicasAsignadas.size() != req.caracteristicaIds().size()) {
                throw new EntityNotFoundException("Una o más características no existen en el catálogo");
            }
        }

        Optional<Propiedad> existenteOpt = propiedadRepository.findByUbicacionAndPisoAndDepto(
                ubicacionNorm, pisoNorm, deptoNorm
        );

        if (existenteOpt.isPresent()) {
            Propiedad existente = existenteOpt.get();

            // Estrategia "link o conflicto": si existe, no se muta desde este flujo.
            boolean coincide = true;
            coincide &= eq(existente.getTipo(), req.tipo());
            coincide &= eq(existente.getSuperficie(), req.superficie());
            coincide &= eq(existente.getAmbientes(), req.ambientes());
            coincide &= eq(existente.getSanitarios(), req.sanitarios());
            coincide &= eq(existente.getExpensas(), req.expensas());
            coincide &= eq(existente.getCaracteristicas(), caracteristicasAsignadas);

            if (!coincide) {
                throw new ConflictException(
                        "La propiedad ya existe con datos distintos; usa la existente o cambia la ubicacion"
                );
            }

            return existente;
        }

        // 2. Si no existe, se crea con los datos provistos
        Propiedad nueva = Propiedad.builder()
                .ubicacion(ubicacionNorm)
                .piso(pisoNorm)
                .depto(deptoNorm)
                .tipo(req.tipo())
                .superficie(req.superficie())
                .ambientes(req.ambientes())
                .sanitarios(req.sanitarios())
                .expensas(req.expensas())
                .caracteristicas(caracteristicasAsignadas)
                .build();

        return propiedadRepository.save(nueva);
    }


    @Transactional()
    public Optional<PropiedadResponse> buscarPorUbicacion(String ubicacion, String piso, String depto, Usuario usuario) {
        String ubicacionNorm = normRequired(ubicacion);
        String pisoNorm = normOptional(piso);
        String deptoNorm = normOptional(depto);

        Optional<Propiedad> propiedadOptional = propiedadRepository.findByUbicacionAndPisoAndDepto(
                ubicacionNorm, pisoNorm, deptoNorm
        );

        return propiedadOptional.map(propiedad -> mapToResponse(propiedad, usuario));
    }

    public PropiedadResponse mapToResponse(Propiedad propiedad, Usuario usuario) {
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
