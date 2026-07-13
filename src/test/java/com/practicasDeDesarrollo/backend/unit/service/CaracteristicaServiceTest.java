package com.practicasDeDesarrollo.backend.unit.service;

import com.practicasDeDesarrollo.backend.dto.response.CaracteristicaResponse;
import com.practicasDeDesarrollo.backend.entity.Caracteristica;
import com.practicasDeDesarrollo.backend.repository.CaracteristicaRepository;
import com.practicasDeDesarrollo.backend.service.CaracteristicaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaracteristicaService — Unit Tests")
class CaracteristicaServiceTest {

    @Mock
    private CaracteristicaRepository caracteristicaRepository;

    @InjectMocks
    private CaracteristicaService caracteristicaService;

    @Nested
    @DisplayName("crear")
    class Crear {

        @Test
        void normaliza_y_persiste() {
            when(caracteristicaRepository.save(any())).thenAnswer(inv -> {
                Caracteristica c = inv.getArgument(0);
                return Caracteristica.builder().id(1L).nombre(c.getNombre()).build();
            });

            CaracteristicaResponse response = caracteristicaService.crear("  cochera  ");

            assertEquals(1L, response.id());
            assertEquals("COCHERA", response.nombre());

            ArgumentCaptor<Caracteristica> captor = ArgumentCaptor.forClass(Caracteristica.class);
            verify(caracteristicaRepository).save(captor.capture());
            assertEquals("COCHERA", captor.getValue().getNombre());
        }

        @Test
        void lanza_npe_si_nombre_null() {
            assertThrows(NullPointerException.class, () -> caracteristicaService.crear(null));
            verifyNoInteractions(caracteristicaRepository);
        }
    }

    @Nested
    @DisplayName("listar")
    class Listar {

        @Test
        void consulta_ordenada_por_nombre_asc() {
            when(caracteristicaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre")))
                    .thenReturn(List.of());

            List<CaracteristicaResponse> res = caracteristicaService.listar();

            assertTrue(res.isEmpty());
            verify(caracteristicaRepository).findAll(Sort.by(Sort.Direction.ASC, "nombre"));
        }

        @Test
        void mapea_id_y_nombre() {
            when(caracteristicaRepository.findAll(any(Sort.class)))
                    .thenReturn(List.of(
                            Caracteristica.builder().id(1L).nombre("COCHERA").build(),
                            Caracteristica.builder().id(2L).nombre("PILETA").build()
                    ));

            List<CaracteristicaResponse> res = caracteristicaService.listar();

            assertEquals(2, res.size());
            assertEquals(1L, res.get(0).id());
            assertEquals("COCHERA", res.get(0).nombre());
            assertEquals(2L, res.get(1).id());
            assertEquals("PILETA", res.get(1).nombre());
        }
    }
}
