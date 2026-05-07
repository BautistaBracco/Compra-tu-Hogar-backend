package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.dto.response.CaracteristicaResponse;
import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.service.CaracteristicaService;
import com.practicasDeDesarrollo.backend.service.ImagenService;
import com.practicasDeDesarrollo.backend.service.PropiedadService;
import com.practicasDeDesarrollo.backend.service.PublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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

    @PostMapping("/imagen")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {

        String url = imagenService.guardar(file);

        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/propiedad/{direccion}")
    public ResponseEntity<PropiedadResponse> obtenerPropiedadPorUbicacion(
            @PathVariable String direccion,
            @RequestParam(required = false) String piso,
            @RequestParam(required = false) String depto) {

        return propiedadService.buscarPorUbicacion(direccion, piso, depto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<List<CaracteristicaResponse>> listarCaracteristicas() {
        return ResponseEntity.ok(caracteristicaService.listar());
    }

    @GetMapping("/publicaciones")
    public ResponseEntity<List<PublicacionResponse>> buscarPublicaciones(
            @RequestParam(required = false) Boolean vendida,
            @RequestParam(required = false) TipoPropiedad tipo,
            @RequestParam(required = false) BigDecimal minPrecio,
            @RequestParam(required = false) BigDecimal maxPrecio,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Integer ambientesMin,
            @RequestParam(required = false) Integer ambientesMax,
            @RequestParam(required = false) Long inmobiliariaId,
            @RequestParam(required = false) List<Long> caracteristicaIds
    ) {
        return ResponseEntity.ok(publicacionService.buscarPublicaciones(
                vendida,
                tipo,
                minPrecio,
                maxPrecio,
                ubicacion,
                ambientesMin,
                ambientesMax,
                inmobiliariaId,
                caracteristicaIds
        ));
    }

}
