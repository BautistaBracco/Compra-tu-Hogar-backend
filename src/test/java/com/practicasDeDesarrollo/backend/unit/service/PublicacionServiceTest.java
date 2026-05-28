package com.practicasDeDesarrollo.backend.unit.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.PublicacionSearchParams;
import com.practicasDeDesarrollo.backend.dto.request.UpdatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Imagen;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.exception.ForbiddenException;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.PropiedadService;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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

    @InjectMocks
    private PublicacionService publicacionService;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private Usuario inmobiliaria(Long id) {
        return Usuario.builder()
                .id(id)
                .nombre("Inmo " + id)
                .email("inmo" + id + "@test.com")
                .password("pass")
                .rol(RolUsuario.INMOBILIARIA)
                .build();
    }

    private Propiedad propiedadDisponible() {
        return Propiedad.builder()
                .id(10L)
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4").depto("A")
                .superficie(50).ambientes(2).sanitarios(1).expensas(15000)
                .vendida(false)
                .caracteristicas(Set.of())
                .build();
    }

    private Propiedad propiedadVendida() {
        Propiedad p = propiedadDisponible();
        p.setVendida(true);
        return p;
    }

    private Publicacion publicacionConDueno(Long pubId, Usuario dueno, Propiedad propiedad) {
        return Publicacion.builder()
                .id(pubId)
                .descripcion("Descripción original")
                .precio(new BigDecimal("85000.00"))
                .inmobiliaria(dueno)
                .propiedad(propiedad)
                .imagenes(new ArrayList<>(List.of(
                        Imagen.builder().id(1L).url("img1.jpg").orden(1).build(),
                        Imagen.builder().id(2L).url("img2.jpg").orden(2).build()
                )))
                .build();
    }

    private CreatePublicacionRequest requestBase(Propiedad ignorado) {
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

    // ─── createPublicacion ────────────────────────────────────────────────────

    @Nested
    @DisplayName("createPublicacion")
    class CreatePublicacion {

        @Test
        @DisplayName("debería persistir la publicación con imágenes en orden correcto")
        void deberia_persistir_con_imagenes_en_orden() {
            Usuario inmo = inmobiliaria(1L);
            Propiedad propiedad = propiedadDisponible();

            when(propiedadService.buscarOCrear(any())).thenReturn(propiedad);
            when(publicacionRepository.findByInmobiliariaIdAndPropiedadId(1L, 10L))
                    .thenReturn(Optional.empty());
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.createPublicacion(requestBase(propiedad), inmo);

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
        @DisplayName("debería lanzar ConflictException si la propiedad ya fue vendida")
        void deberia_lanzar_ConflictException_si_propiedad_vendida() {
            Usuario inmo = inmobiliaria(1L);
            when(propiedadService.buscarOCrear(any())).thenReturn(propiedadVendida());

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> publicacionService.createPublicacion(requestBase(null), inmo));

            assertEquals("La propiedad ya fue vendida", ex.getMessage());
            verify(publicacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("debería lanzar ConflictException si ya existe una publicación para esa propiedad")
        void deberia_lanzar_ConflictException_si_publicacion_duplicada() {
            Usuario inmo = inmobiliaria(1L);
            Propiedad propiedad = propiedadDisponible();
            Publicacion existente = publicacionConDueno(5L, inmo, propiedad);

            when(propiedadService.buscarOCrear(any())).thenReturn(propiedad);
            when(publicacionRepository.findByInmobiliariaIdAndPropiedadId(1L, 10L))
                    .thenReturn(Optional.of(existente));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> publicacionService.createPublicacion(requestBase(propiedad), inmo));

            assertEquals("Ya existe una publicación para esta propiedad", ex.getMessage());
            verify(publicacionRepository, never()).save(any());
        }
    }

    // ─── modificarPublicacion ─────────────────────────────────────────────────

    @Nested
    @DisplayName("modificarPublicacion")
    class ModificarPublicacion {

        @Test
        @DisplayName("debería actualizar descripción y precio")
        void deberia_actualizar_descripcion_y_precio() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            UpdatePublicacionRequest req = new UpdatePublicacionRequest(
                    "Nueva descripción", new BigDecimal("90000.00"), null
            );
            publicacionService.modificarPublicacion(5L, req, inmo);

            assertEquals("Nueva descripción", pub.getDescripcion());
            assertEquals(new BigDecimal("90000.00"), pub.getPrecio());
        }

        @Test
        @DisplayName("debería reducir la lista de imágenes si la nueva es más corta (orphanRemoval)")
        void deberia_reducir_imagenes_si_lista_es_mas_corta() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible()); // 2 imágenes

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            UpdatePublicacionRequest req = new UpdatePublicacionRequest(
                    "Desc", new BigDecimal("85000.00"), List.of("sola.jpg") // 1 imagen
            );
            publicacionService.modificarPublicacion(5L, req, inmo);

            assertEquals(1, pub.getImagenes().size());
            assertEquals("sola.jpg", pub.getImagenes().get(0).getUrl());
        }

        @Test
        @DisplayName("debería agregar imágenes si la nueva lista es más larga")
        void deberia_agregar_imagenes_si_lista_es_mas_larga() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible()); // 2 imágenes

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            UpdatePublicacionRequest req = new UpdatePublicacionRequest(
                    "Desc", new BigDecimal("85000.00"),
                    List.of("a.jpg", "b.jpg", "c.jpg") // 3 imágenes
            );
            publicacionService.modificarPublicacion(5L, req, inmo);

            assertEquals(3, pub.getImagenes().size());
        }

        @Test
        @DisplayName("debería lanzar ForbiddenException si el usuario no es el dueño")
        void deberia_lanzar_excepcion_si_no_es_dueno() {
            Usuario dueno = inmobiliaria(1L);
            Usuario intruso = inmobiliaria(2L);
            Publicacion pub = publicacionConDueno(5L, dueno, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            ForbiddenException ex = assertThrows(ForbiddenException.class,
                    () -> publicacionService.modificarPublicacion(5L,
                            new UpdatePublicacionRequest("Hack", BigDecimal.ONE, null),
                            intruso));

            assertEquals("No tienes permiso sobre esta publicación", ex.getMessage());
            verify(publicacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("debería lanzar EntityNotFoundException si la publicación no existe")
        void deberia_lanzar_excepcion_si_publicacion_no_existe() {
            when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> publicacionService.modificarPublicacion(99L,
                            new UpdatePublicacionRequest("X", BigDecimal.ONE, null),
                            inmobiliaria(1L)));
        }

        @Test
        @DisplayName("no debería tocar imágenes si request.imagenes() es null")
        void no_deberia_tocar_imagenes_si_imagenes_null() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());
            List<Imagen> originales = new ArrayList<>(pub.getImagenes());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(publicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(publicacionMapper.toResponse(any(Publicacion.class), eq(false)))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.modificarPublicacion(5L,
                    new UpdatePublicacionRequest("Desc", new BigDecimal("85000.00"), null),
                    inmo);

            assertEquals(originales.size(), pub.getImagenes().size());
            assertEquals(originales.get(0).getUrl(), pub.getImagenes().get(0).getUrl());
        }
    }

    // ─── eliminarPublicacion ──────────────────────────────────────────────────

    @Nested
    @DisplayName("eliminarPublicacion")
    class EliminarPublicacion {

        @Test
        @DisplayName("debería llamar a delete() cuando el usuario es el dueño")
        void deberia_llamar_delete_si_es_dueno() {
            Usuario inmo = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, inmo, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            publicacionService.eliminarPublicacion(5L, inmo);

            verify(publicacionRepository).delete(pub);
        }

        @Test
        @DisplayName("debería lanzar ForbiddenException y no borrar si el usuario no es dueño")
        void deberia_lanzar_excepcion_y_no_borrar_si_no_es_dueno() {
            Usuario dueno = inmobiliaria(1L);
            Usuario intruso = inmobiliaria(2L);
            Publicacion pub = publicacionConDueno(5L, dueno, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            assertThrows(ForbiddenException.class,
                    () -> publicacionService.eliminarPublicacion(5L, intruso));

            verify(publicacionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("debería lanzar EntityNotFoundException si la publicación no existe")
        void deberia_lanzar_excepcion_si_publicacion_no_existe() {
            when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> publicacionService.eliminarPublicacion(99L, inmobiliaria(1L)));
        }
    }

    // ─── buscarPorId ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("debería consultar si es favorito para el usuario autenticado")
        void deberia_consultar_si_es_favorito() {
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
        @DisplayName("debería mapear con esFavorito=false cuando no está en favoritos")
        void deberia_mapear_con_esFavorito_false() {
            Usuario usuario = inmobiliaria(1L);
            Publicacion pub = publicacionConDueno(5L, usuario, propiedadDisponible());

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));
            when(usuarioRepository.isFavorito(1L, 5L)).thenReturn(false);
            when(publicacionMapper.toResponse(pub, false)).thenReturn(mock(PublicacionResponse.class));

            publicacionService.buscarPorId(5L, usuario);

            verify(publicacionMapper).toResponse(pub, false);
        }

        @Test
        @DisplayName("debería lanzar EntityNotFoundException si no existe")
        void deberia_lanzar_excepcion_si_no_existe() {
            when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> publicacionService.buscarPorId(99L, inmobiliaria(1L)));
        }
    }

    // ─── listarPublicaciones (inmobiliaria) ───────────────────────────────────

    @Nested
    @DisplayName("listarPublicaciones")
    class ListarPublicaciones {

        @Test
        @DisplayName("debería pedir al repo las publicaciones de la inmobiliaria y mapear sin metadata")
        void deberia_listar_y_mapear_sin_metadata() {
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
        @DisplayName("si no hay publicaciones debería devolver lista vacía")
        void si_no_hay_publicaciones_devuelve_vacio() {
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

    // ─── buscarPublicaciones (usuarios) ───────────────────────────────────────

    @Nested
    @DisplayName("buscarPublicaciones")
    class BuscarPublicaciones {

        @Test
        @DisplayName("si el repositorio devuelve vacío no debería consultar favoritos")
        void si_repo_vacio_no_consulta_favoritos() {
            Usuario usuario = inmobiliaria(1L);
            when(publicacionRepository.search(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()
            )).thenReturn(List.of());

            List<PublicacionResponse> res = publicacionService.buscarPublicaciones(
                    new PublicacionSearchParams(null, null, null, null, null, null, null, null, null),
                    usuario
            );

            assertTrue(res.isEmpty());
            verifyNoInteractions(usuarioRepository);
        }

        @Test
        @DisplayName("debería mapear esFavorito por publicación")
        void deberia_mapear_esFavorito_por_publicacion() {
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
                    new PublicacionSearchParams(null, null, null, null, null, null, null, null, null),
                    usuario
            );

            assertEquals(2, res.size());
            verify(publicacionMapper).toResponse(p1, false);
            verify(publicacionMapper).toResponse(p2, true);
        }

        @Test
        @DisplayName("cuando hay característicaIds debería usar matchAll con ids distinct")
        void cuando_hay_caracteristicas_usa_matchAll_distinct() {
            Usuario usuario = inmobiliaria(1L);
            Publicacion p = publicacionConDueno(10L, usuario, propiedadDisponible());

            List<Long> caracsConDuplicados = List.of(1L, 2L, 1L);
            PublicacionSearchParams params = new PublicacionSearchParams(
                    null, null, null, null, null, null, null, null, caracsConDuplicados
            );

            // We only care about which repository method is used and the distinct list size.
            when(publicacionRepository.searchMatchAllCaracteristicas(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), eq(List.of(1L, 2L)), eq(2L)
            )).thenReturn(List.of(p));

            when(usuarioRepository.findFavoritoIdsIn(eq(1L), eq(List.of(10L))))
                    .thenReturn(Set.of());

            lenient().when(publicacionMapper.toResponse(any(Publicacion.class), anyBoolean()))
                    .thenReturn(mock(PublicacionResponse.class));

            publicacionService.buscarPublicaciones(params, usuario);

            verify(publicacionRepository, never()).search(any(), any(), any(), any(), any(), any(), any(), any());
            verify(publicacionRepository).searchMatchAllCaracteristicas(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), eq(List.of(1L, 2L)), eq(2L)
            );
        }
    }
}
