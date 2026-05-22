package com.practicasDeDesarrollo.backend.repository;

import com.practicasDeDesarrollo.backend.config.JpaConfig;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class PropiedadRepositoryTest {

    @Autowired
    private PropiedadRepository propiedadRepository;

    @BeforeEach
    void setUp() {
        // 1. Configuramos un Departamento
        Propiedad depto = Propiedad.builder()
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4")
                .depto("A")
                .superficie(50)
                .ambientes(2)
                .sanitarios(1)
                .expensas(15000)
                .build();

        // 2. Configuramos una Casa
        // Nota: piso y depto toman el valor "" (cadena vacía) por defecto gracias a @Builder.Default en la entidad
        Propiedad casa = Propiedad.builder()
                .tipo(TipoPropiedad.CASA)
                .ubicacion("CALLE FALSA 123")
                .superficie(100)
                .ambientes(4)
                .sanitarios(2)
                .expensas(0)
                .build();

        propiedadRepository.save(depto);
        propiedadRepository.save(casa);
    }

    // ==========================================
    // TESTS PARA DEPARTAMENTOS
    // ==========================================

    @Test
    void findByUbicacionAndPisoAndDepto_cuandoExisteDepto_deberiaRetornarPropiedad() {
        // Act
        Optional<Propiedad> encontrada = propiedadRepository
                .findByUbicacionAndPisoAndDepto("AV. MITRE 123", "4", "A");

        // Assert
        assertTrue(encontrada.isPresent());
        assertEquals(TipoPropiedad.DEPTO, encontrada.get().getTipo());
        assertEquals(50, encontrada.get().getSuperficie());
    }

    @Test
    void guardarPropiedad_conDeptoDuplicado_deberiaLanzarExcepcion() {
        // Arrange
        Propiedad deptoDuplicado = Propiedad.builder()
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4")
                .depto("A")
                .superficie(120)
                .ambientes(4)
                .sanitarios(2)
                .expensas(20000)
                .build();

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            propiedadRepository.saveAndFlush(deptoDuplicado);
        }, "Debería lanzar error por violar la UniqueConstraint de identidad en el Depto");
    }

    // ==========================================
    // TESTS PARA CASAS
    // ==========================================

    @Test
    void findByUbicacionAndPisoAndDepto_cuandoExisteCasa_deberiaRetornarPropiedad() {
        // Act: Buscamos pasando cadenas vacías como piso y depto
        Optional<Propiedad> encontrada = propiedadRepository
                .findByUbicacionAndPisoAndDepto("CALLE FALSA 123", "", "");

        // Assert
        assertTrue(encontrada.isPresent());
        assertEquals(TipoPropiedad.CASA, encontrada.get().getTipo());
        assertEquals(100, encontrada.get().getSuperficie());
    }

    @Test
    void guardarPropiedad_conCasaDuplicada_deberiaLanzarExcepcion() {
        // Arrange: Intentamos crear otra casa en la misma ubicación
        // Al no especificar piso ni depto, tomarán el valor "" (cadena vacía)
        Propiedad casaDuplicada = Propiedad.builder()
                .tipo(TipoPropiedad.CASA)
                .ubicacion("CALLE FALSA 123")
                .superficie(250) // La superficie diferente no evita la restricción de identidad
                .ambientes(6)
                .sanitarios(3)
                .expensas(0)
                .build();

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            propiedadRepository.saveAndFlush(casaDuplicada);
        }, "Debería lanzar error por violar la UniqueConstraint de identidad en la Casa");
    }

    // ==========================================
    // TEST DE NO EXISTENCIA
    // ==========================================

    @Test
    void findByUbicacionAndPisoAndDepto_cuandoNoExiste_deberiaRetornarVacio() {
        // Act
        Optional<Propiedad> encontrada = propiedadRepository
                .findByUbicacionAndPisoAndDepto("AV. MITRE 123", "5", "B");

        // Assert
        assertTrue(encontrada.isEmpty());
    }
}