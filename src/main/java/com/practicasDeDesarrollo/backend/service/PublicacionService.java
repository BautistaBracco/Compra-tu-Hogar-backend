package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.UpdatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.entity.*;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final PropiedadService propiedadService;

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<PublicacionResponse> buscarPublicaciones(
            Boolean vendida,
            TipoPropiedad tipo,
            BigDecimal minPrecio,
            BigDecimal maxPrecio,
            String ubicacion,
            Integer ambientesMin,
            Integer ambientesMax,
            Long inmobiliariaId,
            List<Long> caracteristicaIds
    ) {
        Boolean vendidaEf = vendida != null ? vendida : false;

        List<Long> caracteristicaIdsEf = caracteristicaIds == null
                ? List.of()
                : caracteristicaIds.stream().distinct().toList();

        List<Publicacion> results;
        if (caracteristicaIdsEf.isEmpty()) {
            results = publicacionRepository.search(
                    vendidaEf,
                    tipo,
                    minPrecio,
                    maxPrecio,
                    ubicacion,
                    ambientesMin,
                    ambientesMax,
                    inmobiliariaId
            );
        } else {
            results = publicacionRepository.searchMatchAllCaracteristicas(
                    vendidaEf,
                    tipo,
                    minPrecio,
                    maxPrecio,
                    ubicacion,
                    ambientesMin,
                    ambientesMax,
                    inmobiliariaId,
                    caracteristicaIdsEf,
                    caracteristicaIdsEf.size()
            );
        }

        return results.stream().map(this::mapToResponse).toList();
    }

    public PublicacionResponse createPublicacion(@NonNull CreatePublicacionRequest request, Usuario inmobiliaria) {

        Propiedad propiedad = propiedadService.buscarOCrear(request.propiedad());

        if (Boolean.TRUE.equals(propiedad.getVendida())) {
            throw new ConflictException("La propiedad ya fue vendida; no se puede volver a publicar");
        }

        publicacionRepository.findByInmobiliariaIdAndPropiedadId(inmobiliaria.getId(), propiedad.getId())
                .ifPresent(existing -> {
                    throw new ConflictException("Ya existe una publicacion para esta propiedad; edita la existente");
                });

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

        return mapToResponse(p);
    }

    public PublicacionResponse modificarPublicacion(Long id, UpdatePublicacionRequest request, Usuario inmobiliaria) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publicación no encontrada"));

        if (!p.getInmobiliaria().getId().equals(inmobiliaria.getId())) {
            throw new IllegalArgumentException("No tienes permiso para modificar esta publicación");
        }

        p.setDescripcion(request.descripcion());
        p.setPrecio(request.precio());

        // Este codigo raro es para que hibernate me perimta borrar las imagenes viejas y colocar las nuevas de la publicacion
        if (request.imagenes() != null) {
            List<String> nuevasUrls = request.imagenes();
            List<Imagen> actuales = p.getImagenes();

            // Límite de imágenes que coinciden entre la lista vieja y la nueva
            int limit = Math.min(nuevasUrls.size(), actuales.size());

            // 1. Actualizamos las URLs de las imágenes que ya existen en la base de datos
            for (int i = 0; i < limit; i++) {
                actuales.get(i).setUrl(nuevasUrls.get(i));
            }

            // 2. Si el usuario envió MÁS imágenes, creamos las que faltan al final
            if (nuevasUrls.size() > actuales.size()) {
                for (int i = limit; i < nuevasUrls.size(); i++) {
                    actuales.add(Imagen.builder()
                            .url(nuevasUrls.get(i))
                            .publicacion(p)
                            .orden(i + 1)
                            .build());
                }
            }
            // 3. Si el usuario envió MENOS imágenes, borramos las que sobran al final
            else if (nuevasUrls.size() < actuales.size()) {
                // Borramos desde atrás hacia adelante para no romper los índices del ArrayList
                if (actuales.size() > limit) {
                    actuales.subList(limit, actuales.size()).clear();
                }
            }
        }

        return mapToResponse(publicacionRepository.save(p));
    }

    public void eliminarPublicacion(Long id, @NonNull Usuario inmobiliaria) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publicación no encontrada"));

        if (!p.getInmobiliaria().getId().equals(inmobiliaria.getId())) {
            throw new IllegalArgumentException("No tienes permiso para modificar esta publicación");
        }

        publicacionRepository.delete(p);
    }

    private PublicacionResponse mapToResponse(Publicacion p) {
        Usuario inmobiliaria = p.getInmobiliaria();
        UsuarioResponse inmobiliariaResponse = new UsuarioResponse(
                inmobiliaria.getId(),
                inmobiliaria.getNombre(),
                inmobiliaria.getEmail(),
                inmobiliaria.getIcono()
        );

        Propiedad propiedad = p.getPropiedad();
        Set<String> caracteristicas = propiedad.getCaracteristicas().stream()
                .map(Caracteristica::getNombre)
                .collect(Collectors.toSet());

        PropiedadResponse propiedadResponse = new PropiedadResponse(
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
                caracteristicas
        );

        return new PublicacionResponse(
                p.getId(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getImagenes().stream().map(Imagen::getUrl).toList(),
                inmobiliariaResponse,
                propiedadResponse
        );
    }
}
