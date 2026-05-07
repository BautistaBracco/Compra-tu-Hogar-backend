package com.practicasDeDesarrollo.backend.controller;

import com.practicasDeDesarrollo.backend.service.ImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final ImagenService imagenService;

    @PostMapping("/imagen")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {

        String url = imagenService.guardar(file);

        return ResponseEntity.ok(Map.of("url", url));
    }
}
