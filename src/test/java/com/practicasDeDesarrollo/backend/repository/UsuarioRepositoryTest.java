package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.config.JpaConfig;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Levanta solo la porción de JPA/Hibernate y la DB en memoria (H2)
@ActiveProfiles("test") // Le dice a Spring que use application-test.properties
@Import(JpaConfig.class)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Se ejecuta antes de cada @Test para tener la base de datos limpia y predecible
    @BeforeEach
    void setUp() {
        Usuario inmobiliaria = Usuario.builder()
                .nombre("Inmobiliaria Central")
                .email("central@test.com")
                .password("hash123")
                .rol(RolUsuario.INMOBILIARIA)
                .build();

        Usuario comprador = Usuario.builder()
                .nombre("Juan Perez")
                .email("juan@test.com")
                .password("hash456")
                .rol(RolUsuario.COMPRADOR)
                .build();

        usuarioRepository.save(inmobiliaria);
        usuarioRepository.save(comprador);
    }

    @Test
    void existsByEmail_cuandoEmailExiste_deberiaRetornarTrue() {
        boolean existe = usuarioRepository.existsByEmail("central@test.com");

        assertTrue(existe, "El email debería existir en la base de datos");
    }

    @Test
    void existsByEmail_cuandoEmailNoExiste_deberiaRetornarFalse() {
        boolean existe = usuarioRepository.existsByEmail("fantasma@test.com");

        assertFalse(existe, "El email NO debería existir");
    }

    @Test
    void findByRol_deberiaRetornarSoloUsuariosConEseRol() {
        List<Usuario> inmobiliarias = usuarioRepository.findByRol(RolUsuario.INMOBILIARIA);

        assertEquals(1, inmobiliarias.size(), "Debería encontrar exactamente 1 inmobiliaria");
        assertEquals("Inmobiliaria Central", inmobiliarias.getFirst().getNombre());
    }

    @Test
    void findByEmail_deberiaRetornarUsuarioSiExiste() {
        Optional<Usuario> usuario = usuarioRepository.findByEmail("juan@test.com");

        assertTrue(usuario.isPresent());
        assertEquals(RolUsuario.COMPRADOR, usuario.get().getRol());
    }
}