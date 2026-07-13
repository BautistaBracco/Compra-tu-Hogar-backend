package com.practicasDeDesarrollo.backend.unit.service;

import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.JwtService;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Unit Tests")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void createUsuario_lanza_conflict_si_email_duplicado() {
        when(usuarioRepository.existsByEmail("dup@test.com")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> usuarioService.createUsuario(
                        new CreateUsuarioRequest("Nombre", "dup@test.com", "pass", null),
                        RolUsuario.COMPRADOR
                ));

        assertEquals("El email ya está en uso", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void createUsuario_hashea_persiste_y_genera_token() {
        when(usuarioRepository.existsByEmail("a@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("jwt.token");

        AuthResponse res = usuarioService.createUsuario(
                new CreateUsuarioRequest("A", "a@test.com", "plain", null),
                RolUsuario.COMPRADOR
        );

        assertEquals("jwt.token", res.token());
        assertEquals(10L, res.id());
        assertEquals("a@test.com", res.email());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hashed", captor.getValue().getPassword());
        assertEquals(RolUsuario.COMPRADOR, captor.getValue().getRol());
        verify(jwtService).generateToken(any(Usuario.class));
    }

    @Test
    void login_autentica_y_genera_token() {
        Usuario u = Usuario.builder().id(7L).nombre("U").email("u@test.com").rol(RolUsuario.COMPRADOR).build();
        when(usuarioRepository.findByEmail("u@test.com")).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("jwt");

        AuthResponse res = usuarioService.login("u@test.com", "123456");

        assertEquals("jwt", res.token());
        assertEquals("u@test.com", res.email());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(u);
    }

    @Test
    void login_lanza_notfound_si_usuario_no_existe() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> usuarioService.login("missing@test.com", "pass"));

        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }
}
