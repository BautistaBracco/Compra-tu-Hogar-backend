package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.CreateUsuarioRequest;
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
    public ResponseEntity<Usuario> registrarComprador(@Valid @RequestBody CreateUsuarioRequest request) {
        // El registro público siempre crea usuarios con rol COMPRADOR
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.createUsuario(request, RolUsuario.COMPRADOR));
    }
}