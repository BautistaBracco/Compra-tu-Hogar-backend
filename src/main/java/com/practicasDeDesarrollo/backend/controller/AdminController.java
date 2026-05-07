package com.practicasDeDesarrollo.backend.controller;


import com.practicasDeDesarrollo.backend.dto.request.CreateCaracteristicaRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.dto.response.CaracteristicaResponse;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.service.CaracteristicaService;
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
    private final CaracteristicaService caracteristicaService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(@RequestParam RolUsuario rol) {
        return ResponseEntity.ok(usuarioService.getUsuariosByRol(rol));
    }


    @PostMapping("/inmobiliaria")
    public ResponseEntity<AuthResponse> registrarInmobiliaria(@Valid @RequestBody CreateUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.createUsuario(request, RolUsuario.INMOBILIARIA));
    }

    @PostMapping("/caracteristicas")
    public ResponseEntity<CaracteristicaResponse> crearCaracteristica(
            @Valid @RequestBody CreateCaracteristicaRequest request
    ) {
        CaracteristicaResponse creada = caracteristicaService.crear(request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
