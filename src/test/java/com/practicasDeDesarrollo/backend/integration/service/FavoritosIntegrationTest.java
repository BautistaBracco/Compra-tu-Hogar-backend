package com.practicasDeDesarrollo.backend.integration.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
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
@DisplayName("Favoritos (UsuarioService/PublicacionService) — Integration")
class FavoritosIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PublicacionService publicacionService;

    private Usuario comprador;
    private Long publicacionId;

    @BeforeEach
    void setUp() {
        comprador = crearUsuario("Comprador", "comprador_fav@test.com", RolUsuario.COMPRADOR);
        Usuario inmo = crearUsuario("Inmo", "inmo_fav@test.com", RolUsuario.INMOBILIARIA);

        CreatePropiedadRequest propReq = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "Calle 1", "1", "A", 40, 2, 1, 0, Set.of()
        );
        CreatePublicacionRequest pubReq = new CreatePublicacionRequest(
                "Pub favoritos",
                new BigDecimal("1000.00"),
                List.of("http://img/1.jpg"),
                propReq
        );
        publicacionId = publicacionService.createPublicacion(pubReq, inmo).id();
    }

    private Usuario crearUsuario(String nombre, String email, RolUsuario rol) {
        AuthResponse auth = usuarioService.createUsuario(
                new CreateUsuarioRequest(nombre, email, "123456", null),
                rol
        );
        return Usuario.builder().id(auth.id()).nombre(auth.nombre()).build();
    }

    @Test
    void favoritos_roundtrip() {
        usuarioService.agregarFavorito(publicacionId, comprador);

        PublicacionResponse conFav = publicacionService.buscarPorId(publicacionId, comprador);
        assertNotNull(conFav.metadata());
        assertTrue(Boolean.TRUE.equals(conFav.metadata().esFavorito()));

        usuarioService.eliminarFavorito(publicacionId, comprador);

        PublicacionResponse sinFav = publicacionService.buscarPorId(publicacionId, comprador);
        assertNotNull(sinFav.metadata());
        assertFalse(Boolean.TRUE.equals(sinFav.metadata().esFavorito()));
    }
}
