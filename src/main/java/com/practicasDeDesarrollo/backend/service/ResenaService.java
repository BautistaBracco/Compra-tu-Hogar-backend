package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreateResenaRequest;
import com.practicasDeDesarrollo.backend.dto.response.ResenaResponse;
import com.practicasDeDesarrollo.backend.entity.Resena;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.ResenaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final PublicacionRepository publicacionRepository;

    public ResenaResponse agregarResena(@NonNull Long publicacionId, @NonNull CreateResenaRequest request, Usuario usuario) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new EntityNotFoundException("Publicacion no encontrada"));

        Resena resena = Resena.builder()
                .puntaje(request.puntaje())
                .comentario(request.comentario())
                .autor(usuario)
                .publicacion(publicacion)
                .build();

        Resena saved = resenaRepository.save(resena);
        return this.mapToResponse(saved);
    }

    public List<ResenaResponse> listarResenasPorPublicacion(Long id) {

        return resenaRepository.findByPublicacionId(id).stream()
                .map(this::mapToResponse)
                .toList();

    }

    public void eliminarResenaPorId(Long resenaId, Usuario usuario) {
        Resena resena = resenaRepository.findByIdAndAutorId(resenaId, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada"));

        resenaRepository.delete(resena);
    }

    private ResenaResponse mapToResponse(Resena resena) {
        return new ResenaResponse(
                resena.getId(),
                resena.getPuntaje(),
                resena.getComentario(),
                resena.getAutor().getId(),
                resena.getAutor().getNombre(),
                resena.getPublicacion().getId()
        );
    }
}