package com.practicasDeDesarrollo.backend.integration.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.UpdatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
    private Usuario inmobiliariaIntrusa;

    @BeforeEach
    void setUp() {
        inmobiliariaPersistida = persistir("Quilmes Propiedades", "info@quilmesprop.com", RolUsuario.INMOBILIARIA);
        inmobiliariaIntrusa = persistir("Otra Inmobiliaria", "otra@test.com", RolUsuario.INMOBILIARIA);
    }

    private Usuario persistir(String nombre, String email, RolUsuario rol) {
        return usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .email(email)
                .password("password123")
                .rol(rol)
                .build());
    }

    private PublicacionResponse crearPublicacionBase() {
        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Pringles 450", "2", "B", 40, 2, 1, 12000, Set.of()
        );
        CreatePublicacionRequest pubReq = new CreatePublicacionRequest(
                "Oportunidad inversores",
                new BigDecimal("85000.00"),
                List.of("http://image.com/1.jpg", "http://image.com/2.jpg"),
                propReq
        );
        return publicacionService.createPublicacion(pubReq, inmobiliariaPersistida);
    }

    @Test
    void createPublicacion_deberiaPersistirPublicacionEImagenesEnOrden() {
        PublicacionResponse response = crearPublicacionBase();

        assertNotNull(response.id());
        assertEquals(2, response.imagenes().size());
        assertEquals(inmobiliariaPersistida.getId(), response.inmobiliaria().id());

        Publicacion enDB = publicacionRepository.findById(response.id()).orElseThrow();
        assertEquals(2, enDB.getImagenes().size());
        assertEquals(1, enDB.getImagenes().get(0).getOrden());
        assertEquals("http://image.com/1.jpg", enDB.getImagenes().get(0).getUrl());
    }

    @Test
    void modificarPublicacion_siendoDueno_deberiaActualizarDatosEImagenes() {
        PublicacionResponse pubCreada = crearPublicacionBase();
        UpdatePublicacionRequest updateReq = new UpdatePublicacionRequest(
                "Texto actualizado",
                new BigDecimal("90000.00"),
                List.of("http://image.com/nueva.jpg")
        );

        PublicacionResponse response = publicacionService.modificarPublicacion(
                pubCreada.id(), updateReq, inmobiliariaPersistida
        );

        assertEquals("Texto actualizado", response.descripcion());
        assertEquals(new BigDecimal("90000.00"), response.precio());
        assertEquals(1, response.imagenes().size());

        // Forzamos la sincro con DB para chequear que el orphanRemoval borró las viejas
        publicacionRepository.flush();
        Publicacion enDB = publicacionRepository.findById(pubCreada.id()).orElseThrow();
        assertEquals(1, enDB.getImagenes().size());
        assertEquals("http://image.com/nueva.jpg", enDB.getImagenes().get(0).getUrl());
    }
}
