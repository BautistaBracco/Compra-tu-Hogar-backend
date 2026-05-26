package com.practicasDeDesarrollo.backend.integration.service;

import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUsuario_deberiaHashearPasswordYGenerarToken() {
        // Arrange
        CreateUsuarioRequest request = new CreateUsuarioRequest(
                "Bauti Test", "bauti@test.com", "miPasswordSegura", "icon.png"
        );

        // Act
        AuthResponse response = usuarioService.createUsuario(request, RolUsuario.COMPRADOR);

        // Assert
        assertNotNull(response.token());
        assertEquals("bauti@test.com", response.email());

        // Verificamos en DB que la password NO es "miPasswordSegura" (debe estar hasheada)
        Usuario usuarioDB = usuarioRepository.findByEmail("bauti@test.com").orElseThrow();
        assertNotEquals("miPasswordSegura", usuarioDB.getPassword());
        assertTrue(passwordEncoder.matches("miPasswordSegura", usuarioDB.getPassword()));
    }

    @Test
    void login_conCredencialesCorrectas_deberiaRetornarAuthResponse() {
        // Arrange: Registramos un usuario primero
        CreateUsuarioRequest registro = new CreateUsuarioRequest(
                "User Login", "login@test.com", "123456", null
        );
        usuarioService.createUsuario(registro, RolUsuario.COMPRADOR);

        // Act
        AuthResponse response = usuarioService.login("login@test.com", "123456");

        // Assert
        assertNotNull(response.token());
        assertEquals("login@test.com", response.email());
    }

    @Test
    void login_conPasswordIncorrecta_deberiaLanzarExcepcion() {
        // Arrange
        usuarioService.createUsuario(
                new CreateUsuarioRequest("Fail", "fail@test.com", "123456", null),
                RolUsuario.COMPRADOR
        );

        // Act & Assert
        assertThrows(Exception.class, () -> {
            usuarioService.login("fail@test.com", "password_erronea");
        });
    }

    @Test
    void createUsuario_conEmailRepetido_deberiaLanzarIllegalArgumentException() {
        // Arrange
        CreateUsuarioRequest user1 = new CreateUsuarioRequest("Uno", "repetido@test.com", "123", null);
        usuarioService.createUsuario(user1, RolUsuario.COMPRADOR);

        CreateUsuarioRequest user2 = new CreateUsuarioRequest("Dos", "repetido@test.com", "456", null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.createUsuario(user2, RolUsuario.COMPRADOR);
        });
        assertEquals("El email ya está en uso", ex.getMessage());
    }
}