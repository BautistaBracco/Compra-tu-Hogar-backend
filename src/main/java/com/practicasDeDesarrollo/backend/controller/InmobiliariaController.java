package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.request.CreatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.request.UpdatePublicacionRequest;
import com.practicasDeDesarrollo.backend.dto.response.CompraResponse;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.service.CompraService;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inmobiliaria")
@RequiredArgsConstructor
public class InmobiliariaController {

    private final PublicacionService publicacionService;
    private final CompraService compraService;

    @PostMapping("/publicacion")
    public ResponseEntity<PublicacionResponse> crearPublicacion(@Valid @RequestBody CreatePublicacionRequest request, @AuthenticationPrincipal Usuario usuario

    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionService.createPublicacion(request, usuario));
    }

    @PutMapping("/publicacion/{id}")
    public ResponseEntity<PublicacionResponse> modificar(@PathVariable Long id, @Valid @RequestBody UpdatePublicacionRequest request, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(publicacionService.modificarPublicacion(id, request, usuario));
    }

    @DeleteMapping("/publicacion/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        publicacionService.eliminarPublicacion(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/publicacion")
    public ResponseEntity<List<PublicacionResponse>> listarPublicaciones(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(publicacionService.listarPublicaciones(usuario));
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<CompraResponse>> obtenerVentas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(compraService.obtenerVentas(usuario));
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<UsuarioResponse>> obtenerClientes(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(compraService.obtenerClientes(usuario));
    }
}
