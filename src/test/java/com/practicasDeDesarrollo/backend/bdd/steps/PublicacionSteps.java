package com.practicasDeDesarrollo.backend.bdd.steps;

import com.practicasDeDesarrollo.backend.bdd.support.BddPayloads;
import com.practicasDeDesarrollo.backend.bdd.support.BddWorld;
import com.practicasDeDesarrollo.backend.bdd.support.Endpoints;
import com.practicasDeDesarrollo.backend.bdd.support.TestHttpClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class PublicacionSteps {

    private final UserSteps userSteps;
    private final TestHttpClient http;
    private final BddWorld world;

    public PublicacionSteps(UserSteps userSteps, TestHttpClient http, BddWorld world) {
        this.userSteps = userSteps;
        this.http = http;
        this.world = world;
    }

    @When("publico un departamento en {string} piso {string} depto {string}")
    public void publico_un_departamento(String ubicacion, String piso, String depto) {
        world.lastResponse = http.postJson(
                Endpoints.INMO_PUBLICACION,
                world.auth.jwt,
                BddPayloads.publicacionBasica("Publicacion BDD", 85000.00, ubicacion, piso, depto)
        );
        assertEquals(HttpStatus.CREATED, world.lastResponse.getStatusCode());
    }

    @Then("la publicación debería aparecer en los resultados de búsqueda")
    public void la_publicacion_deberia_aparecer_en_los_resultados_de_busqueda() {
        ResponseEntity<String> resp = http.get(Endpoints.USUARIOS_PUBLICACIONES, world.auth.jwt);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("Publicacion BDD"));
    }

    @Given("que existe una publicación de {string}")
    public void que_existe_una_publicacion_de(String nombreInmo) {
        String ownerToken = userSteps.crearInmobiliaria(nombreInmo, "owner@bdd.com").token();

        world.lastResponse = http.postJson(
                Endpoints.INMO_PUBLICACION,
                ownerToken,
                BddPayloads.publicacionBasica("Pub Ajena", 85000.00, "Av. Mitre 123", "2", "A")
        );
        assertEquals(HttpStatus.CREATED, world.lastResponse.getStatusCode());
    }

    @When("{string} intenta modificarla")
    public void intentarModificar(String nombreIntruso) {
        // 1) Setup: crear inmobiliaria intrusa
        String intrusoToken = userSteps.crearInmobiliaria(nombreIntruso, "intruso@bdd.com").token();

        // 2) Extract: id de la publicación apuntada por el último response
        long pubId = world.extractId();

        // 3) Act: intentar modificar
        world.lastResponse = http.putJson(
                Endpoints.inmoPublicacion(pubId),
                intrusoToken,
                BddPayloads.modificacion("Hack", 1.00)
        );
    }

    @Then("debería recibir un error de permisos")
    public void deberia_recibir_un_error_de_permisos() {
        assertNotNull(world.lastResponse);
        assertEquals(HttpStatus.FORBIDDEN, world.lastResponse.getStatusCode());
    }

    @When("consulto el listado de publicaciones sin autenticación")
    public void consulto_el_listado_de_publicaciones_sin_autenticacion() {
        world.lastResponse = http.get(Endpoints.USUARIOS_PUBLICACIONES, null);
    }

    @Then("debería recibir un error de no autenticado")
    public void deberia_recibir_un_error_de_no_autenticado() {
        assertNotNull(world.lastResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, world.lastResponse.getStatusCode());
    }
}
