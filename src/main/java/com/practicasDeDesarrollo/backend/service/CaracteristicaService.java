package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.response.CaracteristicaResponse;
import com.practicasDeDesarrollo.backend.entity.Caracteristica;
import com.practicasDeDesarrollo.backend.repository.CaracteristicaRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CaracteristicaService {

    private final CaracteristicaRepository caracteristicaRepository;

    @Transactional(readOnly = true)
    public List<CaracteristicaResponse> listar() {
        return caracteristicaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"))
                .stream()
                .map(c -> new CaracteristicaResponse(c.getId(), c.getNombre()))
                .toList();
    }

    public CaracteristicaResponse crear(@NonNull String nombre) {
        Caracteristica c = Caracteristica.builder()
                .nombre(normalizarNombre(nombre))
                .build();

        Caracteristica saved = caracteristicaRepository.save(c);
        return new CaracteristicaResponse(saved.getId(), saved.getNombre());
    }

    private static String normalizarNombre(String nombre) {
        return nombre.trim().toUpperCase(Locale.ROOT);
    }
}
