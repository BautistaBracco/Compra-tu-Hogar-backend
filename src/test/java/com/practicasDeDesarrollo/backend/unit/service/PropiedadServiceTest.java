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

import static com.practicasDeDesarrollo.backend.unit.support.TestFixtures.caracteristica;
import static com.practicasDeDesarrollo.backend.unit.support.TestFixtures.propiedadBase;
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

    @Nested
    @DisplayName("buscarOCrear")
    class BuscarOCrear {

        @Test
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
        void convierte_piso_y_depto_null_a_vacio() {
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
        void devuelve_existente_si_coincide_y_no_guarda() {
            Propiedad existente = propiedadBase(Set.of());
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("AV. MITRE 123", "4", "A"))
                    .thenReturn(Optional.of(existente));

            Propiedad res = propiedadService.buscarOCrear(new CreatePropiedadRequest(
                    TipoPropiedad.DEPTO, "av. mitre 123", "4", "a",
                    50, 2, 1, 15000, Set.of()
            ));

            assertSame(existente, res);
            verify(propiedadRepository, never()).save(any());
        }

        @Test
        void lanza_conflict_si_existe_con_datos_distintos() {
            Propiedad existente = propiedadBase(Set.of());
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("AV. MITRE 123", "4", "A"))
                    .thenReturn(Optional.of(existente));

            ConflictException ex = assertThrows(ConflictException.class, () -> propiedadService.buscarOCrear(
                    new CreatePropiedadRequest(TipoPropiedad.DEPTO, "av. mitre 123", "4", "a",
                            999, 2, 1, 15000, Set.of())
            ));
            assertTrue(ex.getMessage().toLowerCase().contains("datos distintos"));
            verify(propiedadRepository, never()).save(any());
        }

        @Test
        void resuelve_y_asigna_caracteristicas() {
            Caracteristica cochera = caracteristica(1L, "COCHERA");
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
        void lanza_notfound_si_falta_caracteristica_del_catalogo() {
            CreatePropiedadRequest req = new CreatePropiedadRequest(
                    TipoPropiedad.DEPTO, "dir 1", "1", "A",
                    40, 2, 1, 0, Set.of(1L, 99L)
            );
            when(caracteristicaRepository.findAllById(Set.of(1L, 99L)))
                    .thenReturn(List.of(caracteristica(1L, "COCHERA")));

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> propiedadService.buscarOCrear(req));
            assertTrue(ex.getMessage().toLowerCase().contains("caracter"));
            verify(propiedadRepository, never()).save(any());
        }

        @Test
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
        void normaliza_argumentos_antes_de_consultar() {
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("AV. LIBERTAD 1", "3", "C"))
                    .thenReturn(Optional.empty());

            propiedadService.buscarPorUbicacion("  av. libertad 1  ", " 3 ", " c ", null);

            verify(propiedadRepository).findByUbicacionAndPisoAndDepto("AV. LIBERTAD 1", "3", "C");
        }

        @Test
        void retorna_empty_si_no_existe() {
            when(propiedadRepository.findByUbicacionAndPisoAndDepto(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());

            Optional<PropiedadResponse> res = propiedadService.buscarPorUbicacion("Dir", null, null, null);

            assertTrue(res.isEmpty());
            verifyNoInteractions(propiedadMapper);
        }

        @Test
        void mapea_y_retorna_si_existe() {
            Propiedad existente = propiedadBase(Set.of());
            when(propiedadRepository.findByUbicacionAndPisoAndDepto("DIR", "", ""))
                    .thenReturn(Optional.of(existente));
            when(propiedadMapper.toResponse(existente)).thenReturn(new PropiedadResponse(
                    1L, "DIR", "", "", TipoPropiedad.DEPTO, 50, 2, 1, 15000, false, Set.of()
            ));

            Optional<PropiedadResponse> res = propiedadService.buscarPorUbicacion("dir", null, null, null);

            assertTrue(res.isPresent());
            assertEquals(1L, res.get().id());
            verify(propiedadMapper).toResponse(existente);
        }
    }
}
