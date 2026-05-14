package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.request.CreateResenaRequest;
import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;
import com.practicasDeDesarrollo.backend.dto.response.ResenaResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.service.ResenaService;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comprador")
@RequiredArgsConstructor
public class CompradorController {

    private final UsuarioService usuarioService;
    private final ResenaService resenaService;

    @GetMapping("/favoritos")
    public ResponseEntity<List<PropiedadResponse>> listarFavoritos(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.obtenerFavoritos(usuario));
    }

    @PostMapping("/favoritos/{propiedadId}")
    public ResponseEntity<PropiedadResponse> agregarFavorito(
            @PathVariable Long propiedadId,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.agregarFavorito(propiedadId, usuario));
    }

    @DeleteMapping("/favoritos/{propiedadId}")
    public ResponseEntity<Void> eliminarFavorito(
            @PathVariable Long propiedadId,
            @AuthenticationPrincipal Usuario usuario) {
        usuarioService.eliminarFavorito(propiedadId, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reseñas")
    public ResponseEntity<ResenaResponse> agregarResena(
            @Valid @RequestBody CreateResenaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(resenaService.agregarResena(request, usuario));
    }
}

