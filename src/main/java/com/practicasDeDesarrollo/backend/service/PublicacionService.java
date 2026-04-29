package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Imagen;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final PropiedadService propiedadService;

    public PublicacionResponse createPublicacion(@NonNull CreatePublicacionRequest request, Usuario inmobiliaria) {

        Propiedad propiedad = propiedadService.buscarOCrear(request.propiedad());

        Publicacion p = Publicacion.builder()
                .precio(request.precio())
                .descripcion(request.descripcion())
                .inmobiliaria(inmobiliaria)
                .propiedad(propiedad)
                .build();

        // Asociacion de imagenes con publicacion

        List<Imagen> imagenesEntidad = IntStream.range(0, request.imagenes().size())
                .mapToObj(i -> Imagen.builder()
                        .url(request.imagenes().get(i))
                        .publicacion(p)
                        .orden(i + 1)
                        .build())
                .toList();

        p.setImagenes(new ArrayList<>(imagenesEntidad));


        publicacionRepository.save(p);

        return new PublicacionResponse(
                p.getId(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getFueVendida(),
                p.getImagenes().stream().map(Imagen::getUrl).toList(),
                p.getInmobiliaria().getId(),
                p.getPropiedad().getId()
        );
    }
}