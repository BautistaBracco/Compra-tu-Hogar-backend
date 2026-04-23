package com.practicasDeDesarrollo.backend.controller;


import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @PostMapping("/inmobiliaria")
    public ResponseEntity<AuthResponse> registrarInmobiliaria(@Valid @RequestBody CreateUsuarioRequest request) {
        // Forzamos que el rol sea INMOBILIARIA
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.createUsuario(request, RolUsuario.INMOBILIARIA));
    }
}
