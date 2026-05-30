package com.practicasDeDesarrollo.backend.mapper;

import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioMetadataResponse;
import com.practicasDeDesarrollo.backend.entity.Imagen;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PublicacionMapper {

    private final UsuarioMapper usuarioMapper;
    private final PropiedadMapper propiedadMapper;

    public PublicacionResponse toResponse(Publicacion publicacion) {
        return toResponse(publicacion, null);
    }

    public PublicacionResponse toResponse(
            Publicacion publicacion,
            Boolean esFavorito
    ) {
        if (publicacion == null) return null;

        List<String> urlsImagenes = publicacion.getImagenes() != null
                ? publicacion.getImagenes()
                .stream()
                .map(Imagen::getUrl)
                .toList()
                : List.of();

        UsuarioMetadataResponse viewer = esFavorito != null
                ? new UsuarioMetadataResponse(esFavorito)
                : null;

        return new PublicacionResponse(
                publicacion.getId(),
                publicacion.getDescripcion(),
                publicacion.getPrecio(),
                urlsImagenes,
                usuarioMapper.toResponse(publicacion.getInmobiliaria()),
                propiedadMapper.toResponse(publicacion.getPropiedad()),
                viewer
        );
    }
}
