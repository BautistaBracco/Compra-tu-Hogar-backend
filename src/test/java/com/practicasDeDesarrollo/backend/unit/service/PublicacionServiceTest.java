package com.practicasDeDesarrollo.backend.unit.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.PublicacionSearchParams;
import com.practicasDeDesarrollo.backend.dto.request.UpdatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.exception.ForbiddenException;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.PropiedadService;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.practicasDeDesarrollo.backend.unit.support.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicacionService — Unit Tests")
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;
    @Mock
    private PropiedadService propiedadService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PublicacionMapper publicacionMapper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterRegistry meterRegistry;

    @InjectMocks
    private PublicacionService publicacionService;

    private CreatePublicacionRequest requestBase() {
        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "av. mitre 123", "4", "a", 50, 2, 1, 15000, Set.of()
        );
        return new CreatePublicacionRequest(
                "Oportunidad",
                new BigDecimal("85000.00"),
                List.of("img1.jpg", "img2.jpg"),
                propReq
        );
    }

    @Nested
    @DisplayName("createPublicacion")
    class CreatePublicacion {

        @Test
        void deberia_persistir_con_imagenes_en_orden() {
            Usuario inmo = inmobiliaria(1L);
            Propiedad propiedad = propiedadDisponible();

            when(propiedadService.buscarOCrear(any())).thenReturn(propiedad);
            when(publicacionRepository.findByInmobiliariaIdAndPropiedadId(1L, 10L))
                    .thenReturn(Optional.empty());
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.createPublicacion(requestBase(), inmo);

            ArgumentCaptor<Publicacion> captor = ArgumentCaptor.forClass(Publicacion.class);
            verify(publicacionRepository).save(captor.capture());
            Publicacion guardada = captor.getValue();

            assertEquals(2, guardada.getImagenes().size());
            assertEquals(1, guardada.getImagenes().get(0).getOrden());
            assertEquals("img1.jpg", guardada.getImagenes().get(0).getUrl());
            assertSame(guardada, guardada.getImagenes().get(0).getPublicacion());
            assertEquals(2, guardada.getImagenes().get(1).getOrden());
            assertEquals("img2.jpg", guardada.getImagenes().get(1).getUrl());
            assertSame(guardada, guardada.getImagenes().get(1).getPublicacion());
        }

        @Test
        void lanza_conflict_si_propiedad_vendida() {
            when(propiedadService.buscarOCrear(any())).thenReturn(propiedadVendida());

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> publicacionService.createPublicacion(requestBase(), inmobiliaria(1L)));

            assertEquals("La propiedad ya fue vendida", ex.getMessage());
            verify(publicacionRepository, never()).save(any());
        }

        @Test
        void lanza_conflict_si_publicacion_duplicada() {
            Usuario inmo = inmobiliaria(1L);
            Propiedad propiedad = propiedadDisponible();
            Publicacion existente = publicacionConDueno(5L, inmo, propiedad);

            when(propiedadService.buscarOCrear(any())).thenReturn(propiedad);
            when(publicacionRepository.findByInmobiliariaIdAndPropiedadId(1L, 10L))
                    .thenReturn(Optional.of(existente));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> publicacionService.createPublicacion(requestBase(), inmo));

            assertEquals("Ya hiciste una publicación para esta propiedad", ex.getMessage());
            verify(publicacionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("modificarPublicacion")
    class ModificarPublicacion {

        @Test
        void actualiza_descripcion_y_precio() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.modificarPublicacion(5L,
                    new UpdatePublicacionRequest("Nueva descripción", new BigDecimal("90000.00"), null),
                    inmo);

            assertEquals("Nueva descripción", pub.getDescripcion());
            assertEquals(new BigDecimal("90000.00"), pub.getPrecio());
        }

        @Test
        void reduce_imagenes_si_lista_es_mas_corta() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.modificarPublicacion(5L,
                    new UpdatePublicacionRequest("Desc", new BigDecimal("85000.00"), List.of("sola.jpg")),
                    inmo);

            assertEquals(1, pub.getImagenes().size());
            assertEquals("sola.jpg", pub.getImagenes().get(0).getUrl());
        }

        @Test
        void agrega_imagenes_si_lista_es_mas_larga() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.modificarPublicacion(5L,
                    new UpdatePublicacionRequest("Desc", new BigDecimal("85000.00"),
                            List.of("a.jpg", "b.jpg", "c.jpg")),
                    inmo);

            assertEquals(3, pub.getImagenes().size());
        }

        @Test
        void lanza_forbidden_si_no_es_dueno() {
            Publicacion pub = publicacionConDueno(5L, inmobiliaria(1L), propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            ForbiddenException ex = assertThrows(ForbiddenException.class,
                    () -> publicacionService.modificarPublicacion(5L,
                            new UpdatePublicacionRequest("Hack", BigDecimal.ONE, null),
                            inmobiliaria(2L)));

            assertEquals("No tienes permiso sobre esta publicación", ex.getMessage());
            verify(publicacionRepository, never()).save(any());
        }

        @Test
        void lanza_notfound_si_publicacion_no_existe() {
            when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> publicacionService.modificarPublicacion(99L,
                            new UpdatePublicacionRequest("X", BigDecimal.ONE, null),
                            inmobiliaria(1L)));
        }

        @Test
        void no_toca_imagenes_si_request_null() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());
            List<String> originales = pub.getImagenes().stream().map(i -> i.getUrl()).toList();

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.modificarPublicacion(5L,
                    new UpdatePublicacionRequest("Desc", new BigDecimal("85000.00"), null),
                    inmo);

            assertEquals(originales.size(), pub.getImagenes().size());
            assertEquals(originales, pub.getImagenes().stream().map(i -> i.getUrl()).toList());
        }
    }

    @Nested
    @DisplayName("eliminarPublicacion")
    class EliminarPublicacion {

        @Test
        void llama_delete_si_es_dueno() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            publicacionService.eliminarPublicacion(5L, inmo);

            verify(publicacionRepository).delete(pub);
        }

        @Test
        void lanza_forbidden_y_no_borra_si_no_es_dueno() {
            Publicacion pub = publicacionConDueno(5L, inmobiliaria(1L), propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            assertThrows(ForbiddenException.class,
                    () -> publicacionService.eliminarPublicacion(5L, inmobiliaria(2L)));

            verify(publicacionRepository, never()).delete(any());
        }

        @Test
        void lanza_notfound_si_publicacion_no_existe() {
            when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> publicacionService.eliminarPublicacion(99L, inmobiliaria(1L)));
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        void consulta_si_es_favorito() {
            Usuario usuario = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, usuario, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(usuarioRepository.isFavorito(1L, 5L)).thenReturn(true);
            when(publicacionMapper.toResponse(pub, true)).thenReturn(mock(PublicacionResponse.class));

            publicacionService.buscarPorId(5L, usuario);

            verify(usuarioRepository).isFavorito(1L, 5L);
            verify(publicacionMapper).toResponse(pub, true);
        }

        @Test
        void mapea_con_esFavorito_false() {
            Usuario usuario = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, usuario, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(usuarioRepository.isFavorito(1L, 5L)).thenReturn(false);
            when(publicacionMapper.toResponse(pub, false)).thenReturn(mock(PublicacionResponse.class));

            publicacionService.buscarPorId(5L, usuario);

            verify(publicacionMapper).toResponse(pub, false);
        }

        @Test
        void lanza_notfound_si_no_existe() {
            when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> publicacionService.buscarPorId(99L, inmobiliaria(1L)));
        }
    }

    @Nested
    @DisplayName("listarPublicaciones")
    class ListarPublicaciones {

        @Test
        void lista_y_mapea_sin_metadata() {
            Usuario inmo = inmobiliaria(7L);
            Publicacion p1 = publicacionConDueno(1L, inmo, propiedadDisponible());
            Publicacion p2 = publicacionConDueno(2L, inmo, propiedadDisponible());

            when(publicacionRepository.search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(7L)
            )).thenReturn(List.of(p1, p2));

            PublicacionResponse r1 = mock(PublicacionResponse.class);
            PublicacionResponse r2 = mock(PublicacionResponse.class);
            when(publicacionMapper.toResponse(p1)).thenReturn(r1);
            when(publicacionMapper.toResponse(p2)).thenReturn(r2);

            List<PublicacionResponse> res = publicacionService.listarPublicaciones(inmo);

            assertEquals(2, res.size());
            verify(publicacionMapper).toResponse(p1);
            verify(publicacionMapper).toResponse(p2);
            verifyNoInteractions(usuarioRepository);
        }

        @Test
        void devuelve_lista_vacia_si_no_hay_publicaciones() {
            Usuario inmo = inmobiliaria(7L);
            when(publicacionRepository.search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(7L)
            )).thenReturn(List.of());

            List<PublicacionResponse> res = publicacionService.listarPublicaciones(inmo);

            assertTrue(res.isEmpty());
            verifyNoInteractions(publicacionMapper);
            verifyNoInteractions(usuarioRepository);
        }
    }

    @Nested
    @DisplayName("buscarPublicaciones")
    class BuscarPublicaciones {

        @Test
        void no_consulta_favoritos_si_repo_vacio() {
            Usuario usuario = inmobiliaria(1L);
            when(publicacionRepository.search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()
            )).thenReturn(List.of());

            List<PublicacionResponse> res = publicacionService.buscarPublicaciones(
                    emptySearch(), usuario
            );

            assertTrue(res.isEmpty());
            verifyNoInteractions(usuarioRepository);
        }

        @Test
        void mapea_esFavorito_por_publicacion() {
            Usuario usuario = inmobiliaria(1L);
            Publicacion p1 = publicacionConDueno(10L, usuario, propiedadDisponible());
            Publicacion p2 = publicacionConDueno(11L, usuario, propiedadDisponible());

            when(publicacionRepository.search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()
            )).thenReturn(List.of(p1, p2));

            when(usuarioRepository.findFavoritoIdsIn(eq(1L), eq(List.of(10L, 11L))))
                    .thenReturn(Set.of(11L));

            PublicacionResponse r1 = mock(PublicacionResponse.class);
            PublicacionResponse r2 = mock(PublicacionResponse.class);
            when(publicacionMapper.toResponse(p1, false)).thenReturn(r1);
            when(publicacionMapper.toResponse(p2, true)).thenReturn(r2);

            List<PublicacionResponse> res = publicacionService.buscarPublicaciones(
                    emptySearch(), usuario
            );

            assertEquals(2, res.size());
            verify(publicacionMapper).toResponse(p1, false);
            verify(publicacionMapper).toResponse(p2, true);
        }

        @Test
        void usa_matchAll_con_ids_distinct_cuando_hay_caracteristicas() {
            Usuario usuario = inmobiliaria(1L);
            Publicacion p = publicacionConDueno(10L, usuario, propiedadDisponible());

            PublicacionSearchParams params = new PublicacionSearchParams(
                    null, null, null, null, null, null, null, null, List.of(1L, 2L, 1L)
            );

            when(publicacionRepository.searchMatchAllCaracteristicas(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), eq(List.of(1L, 2L)), eq(2L)
            )).thenReturn(List.of(p));

            when(usuarioRepository.findFavoritoIdsIn(eq(1L), eq(List.of(10L))))
                    .thenReturn(Set.of());

            when(publicacionMapper.toResponse(any(Publicacion.class), anyBoolean()))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.buscarPublicaciones(params, usuario);

            verify(publicacionRepository, never()).search(any(), any(), any(), any(), any(), any(), any(), any());
            verify(publicacionRepository).searchMatchAllCaracteristicas(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), eq(List.of(1L, 2L)), eq(2L)
            );
        }

        private PublicacionSearchParams emptySearch() {
            return new PublicacionSearchParams(null, null, null, null, null, null, null, null, null);
        }
    }
}
