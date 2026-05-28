package com.practicasDeDesarrollo.backend.unit.service;

import com.practicasDeDesarrollo.backend.dto.response.CompraResponse;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.entity.Compra;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.mapper.UsuarioMapper;
import com.practicasDeDesarrollo.backend.repository.CompraRepository;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.service.CompraService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompraService — Unit Tests")
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;
    @Mock
    private PublicacionRepository publicacionRepository;
    @Mock
    private PublicacionMapper publicacionMapper;
    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private CompraService compraService;

    private Usuario comprador(Long id) {
        return Usuario.builder()
                .id(id)
                .nombre("Comprador " + id)
                .email("comprador" + id + "@test.com")
                .password("pass")
                .rol(RolUsuario.COMPRADOR)
                .build();
    }

    private Usuario inmobiliaria(Long id) {
        return Usuario.builder()
                .id(id)
                .nombre("Inmo " + id)
                .email("inmo" + id + "@test.com")
                .password("pass")
                .rol(RolUsuario.INMOBILIARIA)
                .build();
    }

    private Propiedad propiedad(boolean vendida) {
        return Propiedad.builder()
                .id(10L)
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4").depto("A")
                .superficie(50).ambientes(2).sanitarios(1).expensas(15000)
                .vendida(vendida)
                .caracteristicas(Set.of())
                .build();
    }

    private Publicacion publicacion(Long id, Usuario inmo, Propiedad prop) {
        return Publicacion.builder()
                .id(id)
                .descripcion("Depto amplio")
                .precio(new BigDecimal("85000.00"))
                .inmobiliaria(inmo)
                .propiedad(prop)
                .imagenes(List.of())
                .build();
    }

    @Nested
    @DisplayName("comprar")
    class Comprar {

        @Test
        @DisplayName("persiste la compra, marca vendida y guarda la publicación")
        void persiste_compra_y_marca_vendida() {
            Usuario compradorUsuario = comprador(1L);
            Propiedad prop = propiedad(false);
            Publicacion pub = publicacion(5L, inmobiliaria(2L), prop);

            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            compraService.comprar(5L, compradorUsuario);

            ArgumentCaptor<Compra> compraCaptor = ArgumentCaptor.forClass(Compra.class);
            verify(compraRepository).save(compraCaptor.capture());
            Compra guardada = compraCaptor.getValue();
            assertSame(compradorUsuario, guardada.getComprador());
            assertSame(pub, guardada.getPublicacion());
            assertEquals(new BigDecimal("85000.00"), guardada.getPrecioCompra());

            assertTrue(prop.getVendida());
            verify(publicacionRepository).save(pub);
        }

        @Test
        @DisplayName("lanza ConflictException si ya está vendida y no persiste nada")
        void vendida_lanza_conflict() {
            Propiedad prop = propiedad(true);
            Publicacion pub = publicacion(5L, inmobiliaria(2L), prop);
            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> compraService.comprar(5L, comprador(1L)));

            assertTrue(ex.getMessage().toLowerCase().contains("vendida"));
            verify(compraRepository, never()).save(any());
            verify(publicacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza EntityNotFoundException si la publicación no existe")
        void publicacion_no_existe_lanza() {
            when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> compraService.comprar(99L, comprador(1L)));

            assertTrue(ex.getMessage().toLowerCase().contains("no encontrada"));
            verify(compraRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("obtenerVentas")
    class ObtenerVentas {

        @Test
        @DisplayName("mapea compras de la inmobiliaria")
        void mapea_ventas() {
            Usuario inmo = inmobiliaria(2L);
            Usuario comp = comprador(1L);
            Publicacion pub = publicacion(5L, inmo, propiedad(true));

            Compra compra = Compra.builder()
                    .id(100L)
                    .precioCompra(new BigDecimal("85000.00"))
                    .creadoEn(LocalDateTime.now())
                    .comprador(comp)
                    .publicacion(pub)
                    .build();

            when(compraRepository.findByPublicacionInmobiliaria(inmo)).thenReturn(List.of(compra));
            UsuarioResponse compradorResp = new UsuarioResponse(1L, "Comprador 1", "comprador1@test.com", null);
            when(usuarioMapper.toResponse(comp)).thenReturn(compradorResp);
            when(publicacionMapper.toResponse(pub)).thenReturn(mock(com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse.class));

            List<CompraResponse> res = compraService.obtenerVentas(inmo);

            assertEquals(1, res.size());
            verify(compraRepository).findByPublicacionInmobiliaria(inmo);
            verify(usuarioMapper).toResponse(comp);
            verify(publicacionMapper).toResponse(pub);
        }
    }

    @Nested
    @DisplayName("obtenerClientes")
    class ObtenerClientes {

        @Test
        @DisplayName("devuelve compradores únicos")
        void devuelve_unicos() {
            Usuario inmo = inmobiliaria(2L);
            Usuario comp = comprador(1L);

            Publicacion pub1 = publicacion(5L, inmo, propiedad(true));
            Publicacion pub2 = publicacion(6L, inmo, propiedad(true));

            Compra compra1 = Compra.builder().id(100L).comprador(comp).publicacion(pub1).build();
            Compra compra2 = Compra.builder().id(101L).comprador(comp).publicacion(pub2).build();

            when(compraRepository.findByPublicacionInmobiliaria(inmo)).thenReturn(List.of(compra1, compra2));
            UsuarioResponse compResp = new UsuarioResponse(1L, "Comprador 1", "comprador1@test.com", null);
            when(usuarioMapper.toResponse(comp)).thenReturn(compResp);

            List<UsuarioResponse> res = compraService.obtenerClientes(inmo);

            assertEquals(1, res.size());
        }
    }

    @Nested
    @DisplayName("obtenerCompras")
    class ObtenerCompras {

        @Test
        @DisplayName("mapea compras del comprador")
        void mapea_compras() {
            Usuario comp = comprador(1L);
            Usuario inmo = inmobiliaria(2L);
            Publicacion pub = publicacion(5L, inmo, propiedad(true));

            Compra compra = Compra.builder()
                    .id(100L)
                    .precioCompra(new BigDecimal("85000.00"))
                    .creadoEn(LocalDateTime.now())
                    .comprador(comp)
                    .publicacion(pub)
                    .build();

            when(compraRepository.findByComprador(comp)).thenReturn(List.of(compra));
            when(usuarioMapper.toResponse(comp)).thenReturn(new UsuarioResponse(1L, "Comprador 1", "comprador1@test.com", null));
            when(publicacionMapper.toResponse(pub)).thenReturn(mock(com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse.class));

            List<CompraResponse> res = compraService.obtenerCompras(comp);

            assertEquals(1, res.size());
            verify(compraRepository).findByComprador(comp);
            verify(usuarioMapper).toResponse(comp);
            verify(publicacionMapper).toResponse(pub);
        }
    }
}
