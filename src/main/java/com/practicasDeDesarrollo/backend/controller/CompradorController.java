package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.request.CreateResenaRequest;
import com.practicasDeDesarrollo.backend.dto.response.CompraResponse;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.dto.response.ResenaResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.service.CompraService;
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
    private final CompraService compraService;

    @GetMapping("/favoritos")
    public ResponseEntity<List<PublicacionResponse>> listarFavoritos(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.obtenerFavoritos(usuario));
    }

    @PostMapping("/favoritos/{publicacionId}")
    public ResponseEntity<PublicacionResponse> agregarFavorito(
            @PathVariable Long publicacionId,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.agregarFavorito(publicacionId, usuario));
    }

    @DeleteMapping("/favoritos/{publicacionId}")
    public ResponseEntity<Void> eliminarFavorito(
            @PathVariable Long publicacionId,
            @AuthenticationPrincipal Usuario usuario) {
        usuarioService.eliminarFavorito(publicacionId, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reseñas/{publicacionId}")
    public ResponseEntity<ResenaResponse> agregarResena(
            @PathVariable Long publicacionId,
            @Valid @RequestBody CreateResenaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(resenaService.agregarResena(publicacionId, request, usuario));
    }

    @DeleteMapping("/reseñas/{resenaId}")
    public ResponseEntity<Void> eliminarResena(
            @PathVariable Long resenaId,
            @AuthenticationPrincipal Usuario usuario) {
        resenaService.eliminarResenaPorId(resenaId, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comprar/{publicacionId}")
    public ResponseEntity<Void> comprarInmueble(
            @PathVariable Long publicacionId,
            @AuthenticationPrincipal Usuario usuario) {
        compraService.comprar(publicacionId, usuario);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/compras")
    public ResponseEntity<List<CompraResponse>> listarCompras(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(compraService.obtenerCompras(usuario));
    }
}
