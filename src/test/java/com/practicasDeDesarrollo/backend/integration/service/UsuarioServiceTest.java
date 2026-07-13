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
    void createUsuario_hashea_password_y_genera_token() {
        AuthResponse response = usuarioService.createUsuario(
                new CreateUsuarioRequest("Bauti Test", "bauti@test.com", "miPasswordSegura", "icon.png"),
                RolUsuario.COMPRADOR
        );

        assertNotNull(response.token());
        assertEquals("bauti@test.com", response.email());

        // La password NO debe quedar en texto plano, debe estar hasheada
        Usuario usuarioDB = usuarioRepository.findByEmail("bauti@test.com").orElseThrow();
        assertNotEquals("miPasswordSegura", usuarioDB.getPassword());
        assertTrue(passwordEncoder.matches("miPasswordSegura", usuarioDB.getPassword()));
    }

    @Test
    void login_con_credenciales_correctas_retorna_auth_response() {
        usuarioService.createUsuario(
                new CreateUsuarioRequest("User Login", "login@test.com", "123456", null),
                RolUsuario.COMPRADOR
        );

        AuthResponse response = usuarioService.login("login@test.com", "123456");

        assertNotNull(response.token());
        assertEquals("login@test.com", response.email());
    }
}
