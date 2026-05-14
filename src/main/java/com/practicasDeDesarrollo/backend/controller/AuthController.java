package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.request.LoginRequest;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrarComprador(@Valid @RequestBody CreateUsuarioRequest request) {
        // El registro público siempre crea usuarios con rol COMPRADOR
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.createUsuario(request, RolUsuario.COMPRADOR));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = usuarioService.login(request.email(), request.password());

        return ResponseEntity.ok(response);
    }
}
