package com.practicasDeDesarrollo.backend.bdd.steps;

import com.practicasDeDesarrollo.backend.bdd.support.BddWorld;
import com.practicasDeDesarrollo.backend.bdd.support.TestHttpClient;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

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
        world.lastEmail = email;

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "User");
        body.put("email", email);
        body.put("password", password);
        // Avoid Map.of(..., null) NPE; explicit null is fine for JSON.
        body.put("icono", null);

        ResponseEntity<String> resp = http.postJson(
                "/api/v1/auth/register",
                null,
                body
        );
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    }

    @When("una inmobiliaria se registra con nombre {string}, email {string} y contraseña {string}")
    public void una_inmobiliaria_se_registra_con_nombre_email_y_contrasena(String nombre, String email, String password) {
        // El endpoint público registra COMPRADOR, pero para este escenario solo nos interesa el flujo de auth/token.
        world.lastEmail = email;

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("email", email);
        body.put("password", password);
        body.put("icono", null);

        world.lastResponse = http.postJson(
                "/api/v1/auth/register",
                null,
                body
        );
    }

    @When("intenta hacer login con contraseña {string}")
    public void intenta_hacer_login_con_contrasena(String password) {
        // Use the last email created in the scenario, falling back to the legacy hardcoded one.
        String email = (world.lastEmail != null) ? world.lastEmail : "user@test.com";
        world.lastResponse = http.postJson(
                "/api/v1/auth/login",
                null,
                Map.of(
                        "email", email,
                        "password", password
                )
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
