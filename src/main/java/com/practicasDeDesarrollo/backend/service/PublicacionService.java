package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.PublicacionSearchParams;
import com.practicasDeDesarrollo.backend.dto.request.UpdatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.*;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final PropiedadService propiedadService;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionMapper publicacionMapper;

    @Transactional(readOnly = true)
    public List<PublicacionResponse> buscarPublicaciones(@NonNull PublicacionSearchParams params, @NonNull Usuario usuario) {
        List<Publicacion> resultados = ejecutarBusquedaFiltrada(params);

        if (resultados.isEmpty()) return List.of();

        List<Long> idsPublicaciones = resultados.stream().map(Publicacion::getId).toList();
        Set<Long> favoritosIds = usuarioRepository.findFavoritoIdsIn(usuario.getId(), idsPublicaciones);

        return resultados.stream()
                .map(p -> publicacionMapper.toResponse(p, favoritosIds.contains(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicacionResponse buscarPorId(Long id, Usuario usuario) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publicación no encontrada"));

        boolean esFavorito = usuarioRepository.isFavorito(usuario.getId(), id);
        return publicacionMapper.toResponse(p, esFavorito);
    }

    public PublicacionResponse createPublicacion(@NonNull CreatePublicacionRequest request, Usuario inmobiliaria) {
        Propiedad propiedad = propiedadService.buscarOCrear(request.propiedad());

        validarPublicacionUnica(inmobiliaria, propiedad);

        Publicacion p = Publicacion.builder()
                .precio(request.precio())
                .descripcion(request.descripcion())
                .inmobiliaria(inmobiliaria)
                .propiedad(propiedad)
                .build();

        // Seteo de imágenes con orden
        p.setImagenes(crearListaImagenes(request.imagenes(), p));

        return publicacionMapper.toResponse(publicacionRepository.save(p), false);
    }

    public PublicacionResponse modificarPublicacion(Long id, UpdatePublicacionRequest request, Usuario inmobiliaria) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publicación no encontrada"));

        validarAutoria(p, inmobiliaria);

        p.setDescripcion(request.descripcion());
        p.setPrecio(request.precio());

        if (request.imagenes() != null) {
            actualizarImagenes(p, request.imagenes());
        }

        return publicacionMapper.toResponse(publicacionRepository.save(p), false);
    }

    public void eliminarPublicacion(Long id, @NonNull Usuario inmobiliaria) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publicación no encontrada"));

        validarAutoria(p, inmobiliaria);
        publicacionRepository.delete(p);
    }

    // --- MÉTODOS PRIVADOS DE SOPORTE ---

    private List<Publicacion> ejecutarBusquedaFiltrada(PublicacionSearchParams params) {
        boolean vendida = params.vendida() != null && params.vendida();
        List<Long> caracs = (params.caracteristicaIds() == null)
                ? List.of()
                : params.caracteristicaIds().stream().distinct().toList();

        if (caracs.isEmpty()) {
            return publicacionRepository.search(
                    vendida, params.tipo(), params.minPrecio(), params.maxPrecio(),
                    params.ubicacion(), params.ambientesMin(), params.ambientesMax(), params.inmobiliariaId()
            );
        }

        return publicacionRepository.searchMatchAllCaracteristicas(
                vendida, params.tipo(), params.minPrecio(), params.maxPrecio(),
                params.ubicacion(), params.ambientesMin(), params.ambientesMax(),
                params.inmobiliariaId(), caracs, caracs.size()
        );
    }

    private void actualizarImagenes(Publicacion p, List<String> nuevasUrls) {
        List<Imagen> actuales = p.getImagenes();
        int limit = Math.min(nuevasUrls.size(), actuales.size());

        // Actualizar existentes
        for (int i = 0; i < limit; i++) {
            actuales.get(i).setUrl(nuevasUrls.get(i));
        }

        // Añadir nuevas si sobran
        if (nuevasUrls.size() > actuales.size()) {
            for (int i = limit; i < nuevasUrls.size(); i++) {
                actuales.add(Imagen.builder()
                        .url(nuevasUrls.get(i))
                        .publicacion(p)
                        .orden(i + 1)
                        .build());
            }
        }
        // Eliminar si sobran en la DB
        else if (actuales.size() > limit) {
            actuales.subList(limit, actuales.size()).clear();
        }
    }

    private List<Imagen> crearListaImagenes(List<String> urls, Publicacion p) {
        return new ArrayList<>(IntStream.range(0, urls.size())
                .mapToObj(i -> Imagen.builder()
                        .url(urls.get(i))
                        .publicacion(p)
                        .orden(i + 1)
                        .build())
                .toList());
    }

    private void validarPublicacionUnica(Usuario inmobiliaria, Propiedad propiedad) {
        if (Boolean.TRUE.equals(propiedad.getVendida())) {
            throw new ConflictException("La propiedad ya fue vendida");
        }
        publicacionRepository.findByInmobiliariaIdAndPropiedadId(inmobiliaria.getId(), propiedad.getId())
                .ifPresent(existing -> {
                    throw new ConflictException("Ya existe una publicación para esta propiedad");
                });
    }

    private void validarAutoria(Publicacion p, Usuario inmobiliaria) {
        if (!p.getInmobiliaria().getId().equals(inmobiliaria.getId())) {
            throw new IllegalArgumentException("No tienes permiso sobre esta publicación");
        }
    }


}