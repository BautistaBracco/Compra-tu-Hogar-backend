package com.practicasDeDesarrollo.backend.integration.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.repository.CompraRepository;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import com.practicasDeDesarrollo.backend.service.CompraService;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("CompraService — Integration")
class CompraIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PublicacionService publicacionService;
    @Autowired
    private CompraService compraService;
    @Autowired
    private PublicacionRepository publicacionRepository;
    @Autowired
    private CompraRepository compraRepository;

    private Usuario comprador;
    private Usuario inmobiliaria;
    private Long publicacionId;

    @BeforeEach
    void setUp() {
        var authComprador = usuarioService.createUsuario(
                new com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest(
                        "Comprador",
                        "comprador_compra@test.com",
                        "123456",
                        null
                ),
                RolUsuario.COMPRADOR
        );
        comprador = Usuario.builder().id(authComprador.id()).nombre(authComprador.nombre()).build();

        var authInmo = usuarioService.createUsuario(
                new com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest(
                        "Inmo",
                        "inmo_compra@test.com",
                        "123456",
                        null
                ),
                RolUsuario.INMOBILIARIA
        );
        inmobiliaria = Usuario.builder().id(authInmo.id()).nombre(authInmo.nombre()).build();

        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Calle Compra 1", "1", "A", 40, 2, 1, 0, Set.of()
        );
        CreatePublicacionRequest pubReq = new CreatePublicacionRequest(
                "Pub compra",
                new BigDecimal("85000.00"),
                List.of("http://img/1.jpg"),
                propReq
        );
        PublicacionResponse creada = publicacionService.createPublicacion(pubReq, inmobiliaria);
        publicacionId = creada.id();
        assertNotNull(publicacionId);
    }

    @Test
    @DisplayName("comprar: persiste Compra, marca propiedad vendida, y segunda compra falla")
    void comprar_persiste_y_marca_vendida() {
        compraService.comprar(publicacionId, comprador);

        // Compra persisted and linked uniquely by publicacion_id.
        assertEquals(1, compraRepository.count());

        var pub = publicacionRepository.findById(publicacionId).orElseThrow();
        assertTrue(Boolean.TRUE.equals(pub.getPropiedad().getVendida()));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> compraService.comprar(publicacionId, comprador));
        assertTrue(ex.getMessage().toLowerCase().contains("vendida"));
    }
}
