package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.PeriodoCreateDTO;
import com.example.fairshareapp.model.dto.PeriodoResponseDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Periodo;
import com.example.fairshareapp.model.enums.EstadoPeriodo;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.LiquidacionRepository;
import com.example.fairshareapp.repository.PeriodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para verificar la logica de negocio de PeriodoService:
 * la apertura de periodos evitando duplicados y el cierre disparando la liquidacion.
 */
@ExtendWith(MockitoExtension.class)
class PeriodoServiceTest {

    @Mock
    private PeriodoRepository periodoRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private LiquidacionService liquidacionService;

    @InjectMocks
    private PeriodoService periodoService;

    private Espacio espacio;

    @BeforeEach
    void setUp() {
        espacio = Espacio.builder().id(1L).nombre("Depto Palermo").build();
        lenient().when(liquidacionRepository.findByPeriodoId(org.mockito.ArgumentMatchers.anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void abrirPeriodo_periodoNuevo_seCreaComoAbierto() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(periodoRepository.existsByEspacioIdAndAnioAndMes(1L, 2026, 9)).thenReturn(false);
        when(periodoRepository.save(any(Periodo.class))).thenAnswer(inv -> {
            Periodo periodo = inv.getArgument(0);
            periodo.setId(10L);
            return periodo;
        });

        PeriodoResponseDTO resultado = periodoService.abrirPeriodo(1L,
                PeriodoCreateDTO.builder().anio(2026).mes(9).build());

        assertNotNull(resultado);
        assertEquals(EstadoPeriodo.ABIERTO, resultado.getEstado());
        assertEquals(2026, resultado.getAnio());
        assertEquals(9, resultado.getMes());
    }

    @Test
    void abrirPeriodo_espacioInexistente_lanzaExcepcion() {
        when(espacioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                periodoService.abrirPeriodo(99L, PeriodoCreateDTO.builder().anio(2026).mes(9).build()));
    }

    @Test
    void abrirPeriodo_yaExisteParaEseAnioYMes_lanzaExcepcion() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(periodoRepository.existsByEspacioIdAndAnioAndMes(1L, 2026, 9)).thenReturn(true);

        assertThrows(ReglaInvalidaException.class, () ->
                periodoService.abrirPeriodo(1L, PeriodoCreateDTO.builder().anio(2026).mes(9).build()));
    }

    @Test
    void cerrarPeriodo_periodoAbierto_loMarcaCerradoYGeneraLiquidacion() {
        Periodo periodo = Periodo.builder().id(10L).espacio(espacio).anio(2026).mes(9).estado(EstadoPeriodo.ABIERTO).build();
        LiquidacionResponseDTO liquidacionEsperada = LiquidacionResponseDTO.builder().id(50L).periodoId(10L).build();

        when(periodoRepository.findById(10L)).thenReturn(Optional.of(periodo));
        when(periodoRepository.save(any(Periodo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(liquidacionService.generarYPersistir(any(Periodo.class))).thenReturn(liquidacionEsperada);

        LiquidacionResponseDTO resultado = periodoService.cerrarPeriodo(10L);

        assertEquals(50L, resultado.getId());
        assertEquals(EstadoPeriodo.CERRADO, periodo.getEstado());
        assertNotNull(periodo.getFechaCierre());

        ArgumentCaptor<Periodo> captor = ArgumentCaptor.forClass(Periodo.class);
        verify(liquidacionService).generarYPersistir(captor.capture());
        assertEquals(EstadoPeriodo.CERRADO, captor.getValue().getEstado());
    }

    @Test
    void cerrarPeriodo_yaCerrado_lanzaExcepcion() {
        Periodo periodo = Periodo.builder().id(10L).espacio(espacio).anio(2026).mes(9).estado(EstadoPeriodo.CERRADO).build();
        when(periodoRepository.findById(10L)).thenReturn(Optional.of(periodo));

        assertThrows(ReglaInvalidaException.class, () -> periodoService.cerrarPeriodo(10L));
    }

    @Test
    void cerrarPeriodo_periodoInexistente_lanzaExcepcion() {
        when(periodoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> periodoService.cerrarPeriodo(99L));
    }

    @Test
    void estaFechaEnPeriodoCerrado_conPeriodoCerrado_devuelveTrue() {
        Periodo periodo = Periodo.builder().id(10L).espacio(espacio).anio(2026).mes(9).estado(EstadoPeriodo.CERRADO).build();
        when(periodoRepository.findByEspacioIdAndAnioAndMesAndEstado(1L, 2026, 9, EstadoPeriodo.CERRADO))
                .thenReturn(Optional.of(periodo));

        assertEquals(true, periodoService.estaFechaEnPeriodoCerrado(1L, java.time.LocalDate.of(2026, 9, 15)));
    }

    @Test
    void listarPeriodos_espacioInexistente_lanzaExcepcion() {
        when(espacioRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> periodoService.listarPeriodos(99L));
    }

    @Test
    void listarPeriodos_espacioValido_retornaListaOrdenada() {
        Periodo periodo = Periodo.builder().id(10L).espacio(espacio).anio(2026).mes(9).estado(EstadoPeriodo.ABIERTO).build();
        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(periodoRepository.findByEspacioIdOrderByAnioDescMesDesc(1L)).thenReturn(List.of(periodo));

        List<PeriodoResponseDTO> resultado = periodoService.listarPeriodos(1L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
    }
}
