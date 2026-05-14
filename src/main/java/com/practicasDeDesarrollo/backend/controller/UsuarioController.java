package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.request.UpdateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.request.PublicacionSearchParams;
import com.practicasDeDesarrollo.backend.dto.response.*;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final ImagenService imagenService;
    private final PropiedadService propiedadService;
    private final CaracteristicaService caracteristicaService;
    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;
    private final ResenaService resenaService;

    @PostMapping("/imagen")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {

        String url = imagenService.guardar(file);

        return ResponseEntity.ok(Map.of("url", url));
    }

    @PatchMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @Valid @RequestBody UpdateUsuarioRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(request, usuario));
    }

    @GetMapping("/propiedad/{direccion}")
    public ResponseEntity<PropiedadResponse> obtenerPropiedadPorUbicacion(
            @PathVariable String direccion,
            @RequestParam(required = false) String piso,
            @RequestParam(required = false) String depto,
            @AuthenticationPrincipal Usuario usuario) {
        return propiedadService.buscarPorUbicacion(direccion, piso, depto, usuario)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<List<CaracteristicaResponse>> listarCaracteristicas() {
        return ResponseEntity.ok(caracteristicaService.listar());
    }


    @GetMapping("/publicacion/{id}")
    public ResponseEntity<PublicacionResponse> obtenerPublicacionPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        PublicacionResponse response = publicacionService.buscarPorId(id, usuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/publicaciones")
    public ResponseEntity<List<PublicacionResponse>> buscarPublicaciones(
            @ModelAttribute PublicacionSearchParams params,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(publicacionService.buscarPublicaciones(params, usuario));
    }

    @GetMapping("/publicaciones/{id}/reseñas")
    public ResponseEntity<List<ResenaResponse>> listarResenas(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(resenaService.listarResenasPorPublicacion(id));
    }

}
