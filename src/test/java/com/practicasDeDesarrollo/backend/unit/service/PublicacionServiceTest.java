package com.practicasDeDesarrollo.backend.unit.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private PublicacionMapper publicacionMapper;

    @InjectMocks
    private PublicacionService publicacionService;

    private Usuario usuarioInmobiliaria;
    private Publicacion publicacionMock;

    @BeforeEach
    void setUp() {
        // Arrange general: Datos que usaremos en varios tests
        usuarioInmobiliaria = new Usuario();
        usuarioInmobiliaria.setId(1L);
        usuarioInmobiliaria.setRol(RolUsuario.INMOBILIARIA);

        publicacionMock = new Publicacion();
        publicacionMock.setId(10L);
        publicacionMock.setDescripcion("Publicación de prueba");
        publicacionMock.setPrecio(new java.math.BigDecimal("100000.00"));
        publicacionMock.setImagenes(List.of()); // Si tu entidad tiene imágenes, puedes agregar mocks aquí
        publicacionMock.setInmobiliaria(usuarioInmobiliaria);
    }

    @Test
    void listarPublicaciones_DebeRetornarLista_CuandoUsuarioEsInmobiliaria() {
        // Arrange
        PublicacionResponse responseMock = new PublicacionResponse();
        responseMock.setId(10L);

        when(publicacionRepository.findByInmobiliariaId(usuarioInmobiliaria.getId()))
                .thenReturn(List.of(publicacionMock));

        // Si usas un mapper para convertir la entidad al DTO de respuesta:
        when(publicacionMapper.toResponse(publicacionMock)).thenReturn(responseMock);

        // Act
        List<PublicacionResponse> resultado = publicacionService.listarPublicaciones(usuarioInmobiliaria);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(10L, resultado.get(0).getId()); // Descomentar si usas el mapper mockeado

        // Verifica que el repositorio fue llamado exactamente una vez
        verify(publicacionRepository, times(1)).findByInmobiliariaId(usuarioInmobiliaria.getId());
    }

    @Test
    void eliminarPublicacion_DebeEliminar_CuandoEsPropietario() {
        // Arrange
        when(publicacionRepository.findById(10L)).thenReturn(Optional.of(publicacionMock));
        doNothing().when(publicacionRepository).delete(publicacionMock);

        // Act
        // Asumiendo que tu método recibe el ID de la publicación y el usuario que intenta borrarla
        assertDoesNotThrow(() -> publicacionService.eliminarPublicacion(10L, usuarioInmobiliaria));

        // Assert
        verify(publicacionRepository, times(1)).delete(publicacionMock);
    }

    @Test
    void eliminarPublicacion_DebeLanzarExcepcion_CuandoPublicacionNoExiste() {
        when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            publicacionService.eliminarPublicacion(99L, usuarioInmobiliaria);
        });

        verify(publicacionRepository, never()).delete(any());
    }
}