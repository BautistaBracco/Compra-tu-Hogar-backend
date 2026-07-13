package com.practicasDeDesarrollo.backend.bdd.steps;

import com.practicasDeDesarrollo.backend.bdd.support.BddWorld;
import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import io.cucumber.java.en.Given;

public class UserSteps {

    private final UsuarioService usuarioService;
    private final BddWorld world;

    public UserSteps(UsuarioService usuarioService, BddWorld world) {
        this.usuarioService = usuarioService;
        this.world = world;
    }

    @Given("que estoy autenticado como inmobiliaria")
    public void autenticadoComoInmobiliaria() {
        world.auth.jwt = crearInmobiliaria("Quilmes Prop", "quilmes@bdd.com").token();
    }

    public AuthResponse crearInmobiliaria(String nombre, String email) {
        return usuarioService.createUsuario(
                new CreateUsuarioRequest(nombre, email, "segura123", null),
                RolUsuario.INMOBILIARIA
        );
    }
}
