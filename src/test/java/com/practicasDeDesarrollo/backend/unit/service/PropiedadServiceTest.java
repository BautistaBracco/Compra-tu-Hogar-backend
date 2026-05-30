package com.practicasDeDesarrollo.backend.unit.service;

import com.practicasDeDesarrollo.backend.dto.request.CreatePropiedadRequest;
import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;
import com.practicasDeDesarrollo.backend.entity.Caracteristica;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.enums.TipoPropiedad;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.mapper.PropiedadMapper;
import com.practicasDeDesarrollo.backend.repository.CaracteristicaRepository;
import com.practicasDeDesarrollo.backend.repository.PropiedadRepository;
import com.practicasDeDesarrollo.backend.service.PropiedadService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropiedadService — Unit Tests")
class PropiedadServiceTest {

    @Mock
    private PropiedadRepository propiedadRepository;
    @Mock
    private CaracteristicaRepository caracteristicaRepository;
    @Mock
    private PropiedadMapper propiedadMapper;

    @InjectMocks
    private PropiedadService propiedadService;

    private Propiedad propiedadBase(Set<Caracteristica> caracs) {
        return Propiedad.builder()
                .id(1L)
                .tipo(TipoPropiedad.DEPTO)
                .ubicacion("AV. MITRE 123")
                .piso("4")
                .depto("A")
                .superficie(50)
                .ambientes(2)
                .sanitarios(1)
                .expensas(15000)
                .vendida(false)
                .caracteristicas(caracs)
                .build();
    }

    @Nested
    @DisplayName("buscarOCrear")
    class BuscarOCrear {

        @Test
        @DisplayName("normaliza ubicación/piso/depto y guarda si no existe")
        void normaliza_y_guarda_si_no_existe() {
            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.DEPTO, "  av. mitre 123  ", " 4 ", " a ",
                    50, 2, 1, 15000, Set.of()
            );
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("AV. MITRE 123", "4", "A"))
                    .thenReturn(Optional.empty());
            when(propiedadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Propiedad creada = propiedadService.buscarOCrear(req);

            assertEquals("AV. MITRE 123", creada.getUbicacion());
            assertEquals("4", creada.getPiso());
            assertEquals("A", creada.getDepto());
            verify(propiedadRepository).save(any(Propiedad.class));
            verifyNoInteractions(caracteristicaRepository);
        }

        @Test
        @DisplayName("convierte piso/depto null a string vacío")
        void null_a_vacio_en_piso_y_depto() {
            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.CASA, "Calle Falsa 123", null, null,
                    100, 4, 2, 0, Set.of()
            );
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("CALLE FALSA 123", "", ""))
                    .thenReturn(Optional.empty());
            when(propiedadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Propiedad creada = propiedadService.buscarOCrear(req);

            assertEquals("", creada.getPiso());
            assertEquals("", creada.getDepto());
        }

        @Test
        @DisplayName("si existe y coincide devuelve la existente y no guarda")
        void si_existe_y_coincide_devuelve_existente() {
            Propiedad existente = propiedadBase(Set.of());
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("AV. MITRE 123", "4", "A"))
                    .thenReturn(Optional.of(existente));

            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.DEPTO, "av. mitre 123", "4", "a",
                    50, 2, 1, 15000, Set.of()
            );

            Propiedad res = propiedadService.buscarOCrear(req);

            assertSame(existente, res);
            verify(propiedadRepository, never()).save(any());
        }

        @Test
        @DisplayName("si existe con datos distintos lanza ConflictException")
        void si_existe_con_datos_distintos_lanza_conflict() {
            Propiedad existente = propiedadBase(Set.of());
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("AV. MITRE 123", "4", "A"))
                    .thenReturn(Optional.of(existente));

            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.DEPTO, "av. mitre 123", "4", "a",
                    999, 2, 1, 15000, Set.of()
            );

            ConflictException ex = assertThrows(ConflictException.class, () -> propiedadService.buscarOCrear(req));
            assertTrue(ex.getMessage().toLowerCase().contains("datos distintos"));
            verify(propiedadRepository, never()).save(any());
        }

        @Test
        @DisplayName("resuelve características por IDs y las asigna a la nueva propiedad")
        void resuelve_y_asigna_caracteristicas() {
            Caracteristica cochera = Caracteristica.builder().id(1L).nombre("COCHERA").build();
            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.DEPTO, "dir 2", "2", "B",
                    40, 2, 1, 0, Set.of(1L)
            );
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("DIR 2", "2", "B"))
                    .thenReturn(Optional.empty());
            when(caracteristicaRepository.findAllById(Set.of(1L))).thenReturn(List.of(cochera));
            when(propiedadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Propiedad creada = propiedadService.buscarOCrear(req);

            assertTrue(creada.getCaracteristicas().contains(cochera));
        }

        @Test
        @DisplayName("lanza EntityNotFoundException si falta alguna característica del catálogo")
        void lanza_si_falta_alguna_caracteristica() {
            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.DEPTO, "dir 1", "1", "A",
                    40, 2, 1, 0, Set.of(1L, 99L)
            );
            when(caracteristicaRepository.findAllById(Set.of(1L, 99L)))
                    .thenReturn(List.of(Caracteristica.builder().id(1L).nombre("COCHERA").build()));

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> propiedadService.buscarOCrear(req));
            assertTrue(ex.getMessage().toLowerCase().contains("caracter"));
            verify(propiedadRepository, never()).save(any());
        }

        @Test
        @DisplayName("no consulta características si caracteristicaIds es null")
        void no_consulta_caracteristicas_si_null() {
            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.CASA, "dir 3", null, null,
                    80, 3, 2, 0, null
            );
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("DIR 3", "", ""))
                    .thenReturn(Optional.empty());
            when(propiedadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            propiedadService.buscarOCrear(req);

            verifyNoInteractions(caracteristicaRepository);
        }
    }

    @Nested
    @DisplayName("buscarPorUbicacion")
    class BuscarPorUbicacion {

        @Test
        @DisplayName("normaliza argumentos antes de consultar repositorio")
        void normaliza_argumentos() {
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("AV. LIBERTAD 1", "3", "C"))
                    .thenReturn(Optional.empty());

            propiedadService.buscarPorUbicacion("  av. libertad 1  ", " 3 ", " c ", null);

            verify(propiedadRepository).findByUbicacionAndPisoAndDepto("AV. LIBERTAD 1", "3", "C");
        }

        @Test
        @DisplayName("retorna Optional.empty si no existe")
        void retorna_empty() {
            when(propiedadRepository.findByUbicacionAndPisoAndDepto(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());

            Optional<PropiedadResponse> res = propiedadService.buscarPorUbicacion("Dir", null, null, null);

            assertTrue(res.isEmpty());
            verifyNoInteractions(propiedadMapper);
        }

        @Test
        @DisplayName("mapea y retorna si existe")
        void mapea_y_retorna_si_existe() {
            Propiedad existente = propiedadBase(Set.of());
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("DIR", "", ""))
                    .thenReturn(Optional.of(existente));

            PropiedadResponse resp = new PropiedadResponse(
                    1L, "DIR", "", "", TipoPropiedad.DEPTO, 50, 2, 1, 15000, false, Set.of()
            );
            when(propiedadMapper.toResponse(existente)).thenReturn(resp);

            Optional<PropiedadResponse> res = propiedadService.buscarPorUbicacion("dir", null, null, null);

            assertTrue(res.isPresent());
            assertEquals(1L, res.get().id());
            verify(propiedadMapper).toResponse(existente);
        }
    }
}
