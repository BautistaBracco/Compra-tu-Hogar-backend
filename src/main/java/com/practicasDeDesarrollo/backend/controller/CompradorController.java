package com.practicasDeDesarrollo.backend.controller;


import com.practicasDeDesarrollo.backend.dto.UpdateUsuarioRequest;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/comprador")
@RequiredArgsConstructor
public class CompradorController {

    private final UsuarioService usuarioService;

    // Actualizar perfil propio
    @PutMapping("/perfil/{id}")
    public ResponseEntity<Usuario> actualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        // En una etapa posterior, acá validarías que el ID del token coincida con el ID del path
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

}
