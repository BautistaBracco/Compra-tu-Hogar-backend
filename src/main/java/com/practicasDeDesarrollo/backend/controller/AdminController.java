package com.practicasDeDesarrollo.backend.controller;


import com.practicasDeDesarrollo.backend.dto.request.CreateCaracteristicaRequest;
import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.*;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.service.*;
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
    private final PublicacionService publicacionService;
    private final CompraService compraService;
    private final ResenaService resenaService;
    private final AdminService adminService;

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

    @GetMapping("/reseñas/{usuarioId}")
    public ResponseEntity<List<ResenaResponse>> listarResenasPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(resenaService.listarResenasPorUsuario(usuarioId));
    }

    @GetMapping("/favoritos")
    public ResponseEntity<List<PublicacionResponse>> listarPropiedadesConFavoritos() {
        return ResponseEntity.ok(publicacionService.buscarPublicacionesConAlmenosUnFavorito());
    }

    @GetMapping("/favoritos/{publicacionId}")
    public ResponseEntity<List<UsuarioResponse>> listarUsuariosQueGuardaronPublicacion(@PathVariable Long publicacionId) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioQueGuardaronPublicacion(publicacionId));
    }

    @GetMapping("/compras")
    public ResponseEntity<List<CompraResponse>> listarPropiedadesCompradas() {
        return ResponseEntity.ok(compraService.obtenerVentas());
    }

    @GetMapping("/top-usuarios")
    public ResponseEntity<List<TopUserResponse>> getTopUsuarios() {
        return ResponseEntity.ok(adminService.getTopUsuarios());
    }

    @GetMapping("/top-publicaciones")
    public ResponseEntity<List<TopPropertyResponse>> getTopPropiedades() {
        return ResponseEntity.ok(adminService.getTopPublicaciones());
    }

    @GetMapping("/top-inmobiliarias")
    public ResponseEntity<List<TopAgencyResponse>> getTopInmobiliarias() {
        return ResponseEntity.ok(adminService.getTopInmobiliarias());
    }
}
