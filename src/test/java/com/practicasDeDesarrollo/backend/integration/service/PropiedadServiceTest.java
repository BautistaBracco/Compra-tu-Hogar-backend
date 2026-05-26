package com.practicasDeDesarrollo.backend.integration.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.repository.PropiedadRepository;
import com.practicasDeDesarrollo.backend.service.PropiedadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Usa application-test.properties
@Transactional
class PropiedadServiceTest {

    @Autowired
    private PropiedadService propiedadService;

    @Autowired
    private PropiedadRepository propiedadRepository;

    @Test
    void buscarOCrear_cuandoNoExiste_deberiaGuardarlaEnLaBaseDeDatos() {
        CreatePropiedadRequest req = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, " av. mitre 123  ", " 4 ", "a", 50, 2, 1, 15000, Set.of()
        );

        Propiedad resultado = propiedadService.buscarOCrear(req);

        assertNotNull(resultado.getId(), "La propiedad debería tener un ID asignado por la DB");
        assertEquals("AV. MITRE 123", resultado.getUbicacion(), "El texto debió normalizarse");
        assertEquals("4", resultado.getPiso());
        assertEquals("A", resultado.getDepto());

        assertEquals(1, propiedadRepository.count(), "Debería haber exactamente 1 registro en la tabla");
    }

    @Test
    void buscarOCrear_cuandoYaExiste_deberiaRetornarLaMismaSinDuplicar() {
        Propiedad existente = Propiedad.builder()
                .tipo(TipoPropiedad.CASA)
                .ubicacion("CALLE FALSA 123")
                .piso("")
                .depto("")
                .superficie(100)
                .ambientes(4)
                .sanitarios(2)
                .expensas(0)
                .build();
        propiedadRepository.saveAndFlush(existente);
        Long idOriginal = existente.getId();

        // Armamos un request que coincida (ignorando mayúsculas/espacios)
        CreatePropiedadRequest req = new CreatePropiedadRequest(
                TipoPropiedad.CASA, " calle falsa 123", null, null, 100, 4, 2, 0, Set.of()
        );

        // Act
        Propiedad resultado = propiedadService.buscarOCrear(req);

        // Assert
        assertEquals(idOriginal, resultado.getId(), "Debería retornar exactamente la misma propiedad que ya existía");

        // Verificamos que NO se creó un duplicado en la DB
        assertEquals(1, propiedadRepository.count(), "Debería seguir habiendo solo 1 registro");
    }

    @Test
    void buscarOCrear_cuandoYaExisteConDatosDistintos_deberiaLanzarConflict() {
        Propiedad existente = Propiedad.builder()
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4")
                .depto("A")
                .superficie(50)
                .ambientes(2)
                .sanitarios(1)
                .expensas(15000)
                .build();
        propiedadRepository.saveAndFlush(existente);

        // Mismo identity (ubicacion/piso/depto) pero cambia un atributo intrinseco
        CreatePropiedadRequest req = new CreatePropiedadRequest(
                TipoPropiedad.DEPTO, "av. mitre 123", "4", "a", 60, 2, 1, 15000, Set.of()
        );

        assertThrows(ConflictException.class, () -> propiedadService.buscarOCrear(req));
    }
}
