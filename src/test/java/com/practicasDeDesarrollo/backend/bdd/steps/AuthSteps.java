package com.practicasDeDesarrollo.backend.bdd.steps;

import com.practicasDeDesarrollo.backend.bdd.support.BddPayloads;
import com.practicasDeDesarrollo.backend.bdd.support.BddWorld;
import com.practicasDeDesarrollo.backend.bdd.support.Endpoints;
import com.practicasDeDesarrollo.backend.bdd.support.TestHttpClient;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

public class AuthSteps {

    private final UsuarioRepository usuarioRepository;
    private final TestHttpClient http;
    private final BddWorld world;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthSteps(UsuarioRepository usuarioRepository, TestHttpClient http, BddWorld world) {
        this.usuarioRepository = usuarioRepository;
        this.http = http;
        this.world = world;
    }

    @Given("que no existe una cuenta con el email {string}")
    public void que_no_existe_una_cuenta_con_el_email(String email) {
        assertFalse(usuarioRepository.existsByEmail(email));
    }

    @Given("que existe un usuario con email {string} y contraseña {string}")
    public void que_existe_un_usuario_con_email_y_contrasena(String email, String password) {
        assertEquals(HttpStatus.CREATED, http.postJson(
                Endpoints.AUTH_REGISTER, null, BddPayloads.registroHttp("User", email, password)
        ).getStatusCode());
    }

    @When("se registra con nombre {string}, email {string} y contraseña {string}")
    public void se_registra(String nombre, String email, String password) {
        world.lastResponse = http.postJson(
                Endpoints.AUTH_REGISTER, null, BddPayloads.registroHttp(nombre, email, password)
        );
    }

    @When("intenta hacer login con email {string} y contraseña {string}")
    public void intenta_hacer_login(String email, String password) {
        world.lastResponse = http.postJson(
                Endpoints.AUTH_LOGIN, null, BddPayloads.login(email, password)
        );
    }

    @Then("la respuesta debería contener un token JWT")
    public void la_respuesta_deberia_contener_un_token_jwt() {
        assertNotNull(world.lastResponse);
        assertTrue(world.lastResponse.getStatusCode().is2xxSuccessful());
        String body = world.lastResponse.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"token\""));
    }

    @And("el email del token debería ser {string}")
    public void el_email_del_token_deberia_ser(String email) {
        String body = world.lastResponse.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"email\""));
        assertTrue(body.contains(email));
    }

    @Then("debería recibir un error de autenticación")
    public void deberia_recibir_un_error_de_autenticacion() {
        assertNotNull(world.lastResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, world.lastResponse.getStatusCode());
    }

    @Then("debería recibir un error de conflicto")
    public void deberia_recibir_un_error_de_conflicto() {
        assertNotNull(world.lastResponse);
        assertEquals(HttpStatus.CONFLICT, world.lastResponse.getStatusCode());
    }

    @And("el código de error debería ser {string}")
    public void el_codigo_de_error_deberia_ser(String code) {
        assertNotNull(world.lastResponse);
        String body = world.lastResponse.getBody();
        assertNotNull(body);

        try {
            JsonNode root = objectMapper.readTree(body);
            assertEquals(code, root.path("code").asText());
        } catch (Exception e) {
            throw new AssertionError("No se pudo parsear el ApiError JSON", e);
        }
    }
}
