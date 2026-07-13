package com.practicasDeDesarrollo.backend.unit.service;

import com.practicasDeDesarrollo.backend.dto.response.CompraResponse;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.entity.Compra;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.mapper.UsuarioMapper;
import com.practicasDeDesarrollo.backend.repository.CompraRepository;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.service.CompraService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.practicasDeDesarrollo.backend.unit.support.TestFixtures.*;
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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterRegistry meterRegistry;

    @InjectMocks
    private CompraService compraService;

    private Compra compra(Long id, Usuario comp, Publicacion pub) {
        return Compra.builder()
                .id(id)
                .precioCompra(new BigDecimal("85000.00"))
                .creadoEn(LocalDateTime.now())
                .comprador(comp)
                .publicacion(pub)
                .build();
    }

    private UsuarioResponse compradorResponse() {
        return new UsuarioResponse(1L, "Comprador 1", "comprador1@test.com", null);
    }

    @Nested
    @DisplayName("comprar")
    class Comprar {

        @Test
        void persiste_compra_y_marca_vendida() {
            Usuario compradorUsuario = comprador(1L);
            Propiedad prop = propiedadDisponible();
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
        void lanza_conflict_si_ya_vendida() {
            Publicacion pub = publicacion(5L, inmobiliaria(2L), propiedadVendida());
            when(publicacionRepository.findById(5L)).thenReturn(Optional.of(pub));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> compraService.comprar(5L, comprador(1L)));

            assertTrue(ex.getMessage().toLowerCase().contains("vendida"));
            verify(compraRepository, never()).save(any());
            verify(publicacionRepository, never()).save(any());
        }

        @Test
        void lanza_notfound_si_publicacion_no_existe() {
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
        void mapea_ventas_de_inmobiliaria() {
            Usuario inmo = inmobiliaria(2L);
            Usuario comp = comprador(1L);
            Publicacion pub = publicacion(5L, inmo, propiedadVendida());
            Compra c = compra(100L, comp, pub);

            when(compraRepository.findByPublicacionInmobiliaria(inmo)).thenReturn(List.of(c));
            when(usuarioMapper.toResponse(comp)).thenReturn(compradorResponse());
            when(publicacionMapper.toResponse(pub)).thenReturn(mock(PublicacionResponse.class));

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
        void devuelve_compradores_unicos() {
            Usuario inmo = inmobiliaria(2L);
            Usuario comp = comprador(1L);
            Publicacion pub1 = publicacion(5L, inmo, propiedadVendida());
            Publicacion pub2 = publicacion(6L, inmo, propiedadVendida());

            when(compraRepository.findByPublicacionInmobiliaria(inmo))
                    .thenReturn(List.of(compra(100L, comp, pub1), compra(101L, comp, pub2)));
            when(usuarioMapper.toResponse(comp)).thenReturn(compradorResponse());

            List<UsuarioResponse> res = compraService.obtenerClientes(inmo);

            assertEquals(1, res.size());
        }
    }

    @Nested
    @DisplayName("obtenerCompras")
    class ObtenerCompras {

        @Test
        void mapea_compras_del_comprador() {
            Usuario comp = comprador(1L);
            Usuario inmo = inmobiliaria(2L);
            Publicacion pub = publicacion(5L, inmo, propiedadVendida());
            Compra c = compra(100L, comp, pub);

            when(compraRepository.findByComprador(comp)).thenReturn(List.of(c));
            when(usuarioMapper.toResponse(comp)).thenReturn(compradorResponse());
            when(publicacionMapper.toResponse(pub)).thenReturn(mock(PublicacionResponse.class));

            List<CompraResponse> res = compraService.obtenerCompras(comp);

            assertEquals(1, res.size());
            verify(compraRepository).findByComprador(comp);
            verify(usuarioMapper).toResponse(comp);
            verify(publicacionMapper).toResponse(pub);
        }
    }
}
