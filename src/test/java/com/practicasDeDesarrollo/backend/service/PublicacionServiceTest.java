package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublicacionServiceTest {

    @Autowired
    private PublicacionService publicacionService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PublicacionRepository publicacionRepository;

    private Usuario inmobiliariaPersistida;

    @BeforeEach
    void setUp() {
        // Necesitamos un usuario con rol INMOBILIARIA en la DB para asociar a la publicación
        Usuario inmo = Usuario.builder()
                .nombre("Quilmes Propiedades")
                .email("info@quilmesprop.com")
                .password("password123")
                .rol(RolUsuario.INMOBILIARIA)
                .build();
        inmobiliariaPersistida = usuarioRepository.save(inmo);
    }

    @Test
    void createPublicacion_deberiaPersistirPublicacionEImagenesEnOrden() {
        // Arrange
        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Pringles 450", "2", "B", 40, 2, 1, 12000
        );

        CreatePublicacionRequest pubReq = new CreatePublicacionRequest(
                "Oportunidad inversores",
                new BigDecimal("85000.00"),
                List.of("http://image.com/1.jpg", "http://image.com/2.jpg"),
                propReq
        );

        // Act
        PublicacionResponse response = publicacionService.createPublicacion(pubReq, inmobiliariaPersistida);

        // Assert: Respuesta del servicio
        assertNotNull(response.id());
        assertEquals(2, response.imagenes().size());
        assertEquals(inmobiliariaPersistida.getId(), response.inmobiliariaId());

        // Assert: Verificación en la Base de Datos real
        Publicacion enDB = publicacionRepository.findById(response.id()).orElseThrow();

        assertEquals(2, enDB.getImagenes().size());

        // Verificamos que el orden (i + 1) de tu lógica se guardó bien
        assertEquals(1, enDB.getImagenes().get(0).getOrden());
        assertEquals("http://image.com/1.jpg", enDB.getImagenes().get(0).getUrl());

        assertEquals(2, enDB.getImagenes().get(1).getOrden());
        assertEquals("http://image.com/2.jpg", enDB.getImagenes().get(1).getUrl());

        // Verificamos que la relación bidireccional funciona
        assertEquals(enDB, enDB.getImagenes().get(0).getPublicacion());
    }
}