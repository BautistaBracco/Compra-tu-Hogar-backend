package com.practicasDeDesarrollo.backend.controller;


import com.practicasDeDesarrollo.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/comprador")
@RequiredArgsConstructor
public class CompradorController {

    private final UsuarioService usuarioService;


}
