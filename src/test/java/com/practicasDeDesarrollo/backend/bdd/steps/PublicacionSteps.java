package com.practicasDeDesarrollo.backend.bdd.steps;

import com.practicasDeDesarrollo.backend.bdd.support.BddWorld;
import com.practicasDeDesarrollo.backend.bdd.support.TestHttpClient;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.service.UsuarioService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PublicacionSteps {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final TestHttpClient http;
    private final BddWorld world;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PublicacionSteps(UsuarioService usuarioService, UsuarioRepository usuarioRepository, TestHttpClient http, BddWorld world) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.http = http;
        this.world = world;
    }

    @Given("que estoy autenticado como inmobiliaria")
    public void que_estoy_autenticado_como_inmobiliaria() {
        // Creamos una inmobiliaria directamente via service (evitamos depender de /admin y authz del admin).
        var auth = usuarioService.createUsuario(
                new com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest(
                        "Quilmes Prop",
                        "quilmes@bdd.com",
                        "segura123",
                        null
                ),
                RolUsuario.INMOBILIARIA
        );
        world.jwt = auth.token();
        assertNotNull(world.jwt);
    }

    @When("publico un departamento en {string} piso {string} depto {string}")
    public void publico_un_departamento(String ubicacion, String piso, String depto) {
        world.lastResponse = http.postJson(
                "/api/v1/inmobiliaria/publicacion",
                world.jwt,
                Map.of(
                        "descripcion", "Publicacion BDD",
                        "precio", 85000.00,
                        "imagenes", List.of("http://image.com/1.jpg"),
                        "propiedad", Map.of(
                                "tipo", "DEPTO",
                                "ubicacion", ubicacion,
                                "piso", piso,
                                "depto", depto,
                                "superficie", 40,
                                "ambientes", 2,
                                "sanitarios", 1,
                                "expensas", 12000,
                                "caracteristicaIds", List.of()
                        )
                )
        );
        assertEquals(HttpStatus.CREATED, world.lastResponse.getStatusCode());
    }

    @Then("la publicación debería aparecer en los resultados de búsqueda")
    public void la_publicacion_deberia_aparecer_en_los_resultados_de_busqueda() {
        // Endpoint de búsqueda requiere auth (SecurityConfig protege /usuarios/**). Reutilizamos el mismo JWT.
        ResponseEntity<String> resp = http.get("/api/v1/usuarios/publicaciones", world.jwt);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("Publicacion BDD"));
    }

    @Given("que existe una publicación de {string}")
    public void que_existe_una_publicacion_de(String nombreInmo) {
        // Creamos inmobiliaria dueña y publicación.
        var auth = usuarioService.createUsuario(
                new com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest(
                        nombreInmo,
                        "owner@bdd.com",
                        "segura123",
                        null
                ),
                RolUsuario.INMOBILIARIA
        );
        String ownerToken = auth.token();

        ResponseEntity<String> createResp = http.postJson(
                "/api/v1/inmobiliaria/publicacion",
                ownerToken,
                Map.of(
                        "descripcion", "Pub Ajena",
                        "precio", 85000.00,
                        "imagenes", List.of("http://image.com/1.jpg"),
                        "propiedad", Map.of(
                                "tipo", "DEPTO",
                                "ubicacion", "Av. Mitre 123",
                                "piso", "2",
                                "depto", "A",
                                "superficie", 40,
                                "ambientes", 2,
                                "sanitarios", 1,
                                "expensas", 12000,
                                "caracteristicaIds", List.of()
                        )
                )
        );
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        world.lastResponse = createResp;
    }

    @When("{string} intenta modificarla")
    public void otra_inmobiliaria_intenta_modificarla(String nombreIntruso) {
        // Creamos intruso y obtenemos JWT.
        var auth = usuarioService.createUsuario(
                new com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest(
                        nombreIntruso,
                        "intruso@bdd.com",
                        "segura123",
                        null
                ),
                RolUsuario.INMOBILIARIA
        );
        String intrusoToken = auth.token();

        String body = world.lastResponse.getBody();
        assertNotNull(body);

        long pubId;
        try {
            JsonNode root = objectMapper.readTree(body);
            pubId = root.path("id").asLong(-1);
        } catch (Exception e) {
            throw new AssertionError("No se pudo parsear la respuesta JSON para extraer id", e);
        }
        assertTrue(pubId > 0, "Respuesta debería incluir id > 0");

        world.lastResponse = http.putJson(
                "/api/v1/inmobiliaria/publicacion/" + pubId,
                intrusoToken,
                Map.of(
                        "descripcion", "Hack",
                        "precio", 1.00,
                        "imagenes", List.of()
                )
        );
    }

    @Then("debería recibir un error de permisos")
    public void deberia_recibir_un_error_de_permisos() {
        assertNotNull(world.lastResponse);
        assertEquals(HttpStatus.FORBIDDEN, world.lastResponse.getStatusCode());
    }

    @When("consulto el listado de publicaciones sin autenticación")
    public void consulto_el_listado_de_publicaciones_sin_autenticacion() {
        world.lastResponse = http.get("/api/v1/usuarios/publicaciones", null);
    }

    @Then("debería recibir un error de no autenticado")
    public void deberia_recibir_un_error_de_no_autenticado() {
        assertNotNull(world.lastResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, world.lastResponse.getStatusCode());
    }
}
