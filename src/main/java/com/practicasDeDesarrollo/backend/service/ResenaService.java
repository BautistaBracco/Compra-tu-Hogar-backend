package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreateResenaRequest;
import com.practicasDeDesarrollo.backend.dto.response.ResenaResponse;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Resena;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.repository.PropiedadRepository;
import com.practicasDeDesarrollo.backend.repository.ResenaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final PropiedadRepository propiedadRepository;

    public ResenaResponse agregarResena(@NonNull CreateResenaRequest request, Usuario usuario) {
        Propiedad propiedad = propiedadRepository.findById(request.propiedadId())
                .orElseThrow(() -> new EntityNotFoundException("Propiedad no encontrada"));

        Resena resena = Resena.builder()
                .puntaje(request.puntaje())
                .comentario(request.comentario())
                .autor(usuario)
                .propiedad(propiedad)
                .build();

        Resena saved = resenaRepository.save(resena);
        return new ResenaResponse(saved.getId(), saved.getPuntaje(), saved.getComentario(), saved.getAutor().getId(), saved.getPropiedad().getId());
    }
}
