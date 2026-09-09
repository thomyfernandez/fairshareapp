package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.ProcesarLiquidacionDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.Liquidacion;
import com.example.fairshareapp.model.enums.EstadoGasto;
import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.LiquidacionRepository;
import com.example.fairshareapp.service.impl.LiquidacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para validar la logica transaccional de LiquidacionService.
 * Cubre el control presupuestario de stock, deduccion de saldo y transicion de estados de gastos.
 */
@ExtendWith(MockitoExtension.class)
class LiquidacionServiceTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private GastoRepository gastoRepository;

    @InjectMocks
    private LiquidacionServiceImpl liquidacionService;

    private Espacio espacio;
    private Gasto gasto1;
    private Gasto gasto2;

    @BeforeEach
    void setUp() {
        espacio = Espacio.builder()
                .id(1L)
                .nombre("Departamento Centro")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .presupuestoBase(BigDecimal.valueOf(100000.00))
                .build();

        gasto1 = Gasto.builder()
                .id(10L)
                .descripcion("Internet y Cable")
                .monto(BigDecimal.valueOf(20000.00))
                .fecha(LocalDate.of(2026, 9, 1))
                .espacio(espacio)
                .estado(EstadoGasto.PENDIENTE)
                .build();

        gasto2 = Gasto.builder()
                .id(11L)
                .descripcion("Limpieza")
                .monto(BigDecimal.valueOf(30000.00))
                .fecha(LocalDate.of(2026, 9, 3))
                .espacio(espacio)
                .estado(EstadoGasto.PENDIENTE)
                .build();
    }

    /**
     * Valida el checkout exitoso: descuento de presupuesto en espacio y transicion de gastos a LIQUIDADO.
     */
    @Test
    void procesarCierreLiquidacion_Exitoso_DescuentaPresupuestoYCambiaEstado() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(gastoRepository.findByEspacioIdAndEstadoOrderByFechaAsc(1L, EstadoGasto.PENDIENTE))
                .thenReturn(new ArrayList<>(List.of(gasto1, gasto2)));
        when(liquidacionRepository.save(any(Liquidacion.class))).thenAnswer(i -> {
            Liquidacion l = i.getArgument(0);
            l.setId(100L);
            return l;
        });

        ProcesarLiquidacionDTO dto = ProcesarLiquidacionDTO.builder()
                .mes(9)
                .anio(2026)
                .descripcion("Cierre Septiembre")
                .build();

        LiquidacionResponseDTO response = liquidacionService.procesarCierreLiquidacion(1L, dto);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BigDecimal.valueOf(50000.00), response.getMontoTotal());
        assertEquals(2, response.getCantidadGastos());
        assertEquals(BigDecimal.valueOf(50000.00), espacio.getPresupuestoBase());
        assertEquals(EstadoGasto.LIQUIDADO, gasto1.getEstado());
        assertEquals(EstadoGasto.LIQUIDADO, gasto2.getEstado());
        assertNotNull(gasto1.getLiquidacion());
        assertNotNull(gasto2.getLiquidacion());

        verify(espacioRepository).save(espacio);
        verify(gastoRepository).saveAll(any());
        verify(liquidacionRepository).save(any(Liquidacion.class));
    }

    /**
     * Valida que se rechace el cierre si el monto acumulado supera el presupuesto base disponible.
     */
    @Test
    void procesarCierreLiquidacion_PresupuestoInsuficiente_LanzaReglaInvalidaException() {
        espacio.setPresupuestoBase(BigDecimal.valueOf(40000.00)); // Insuficiente para cubrir 50000

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(gastoRepository.findByEspacioIdAndEstadoOrderByFechaAsc(1L, EstadoGasto.PENDIENTE))
                .thenReturn(List.of(gasto1, gasto2));

        ProcesarLiquidacionDTO dto = new ProcesarLiquidacionDTO();

        ReglaInvalidaException ex = assertThrows(ReglaInvalidaException.class,
                () -> liquidacionService.procesarCierreLiquidacion(1L, dto));

        assertTrue(ex.getMessage().contains("supera el presupuesto disponible"));
        verify(espacioRepository, never()).save(any());
        verify(liquidacionRepository, never()).save(any());
    }

    /**
     * Valida que se rechace la operacion si no existen gastos pendientes a liquidar.
     */
    @Test
    void procesarCierreLiquidacion_SinGastosPendientes_LanzaReglaInvalidaException() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(gastoRepository.findByEspacioIdAndEstadoOrderByFechaAsc(1L, EstadoGasto.PENDIENTE))
                .thenReturn(List.of());

        ProcesarLiquidacionDTO dto = new ProcesarLiquidacionDTO();

        assertThrows(ReglaInvalidaException.class,
                () -> liquidacionService.procesarCierreLiquidacion(1L, dto));
    }

    /**
     * Valida que se lance RecursoNoEncontradoException si el espacio no existe.
     */
    @Test
    void procesarCierreLiquidacion_EspacioInexistente_LanzaRecursoNoEncontradoException() {
        when(espacioRepository.findById(99L)).thenReturn(Optional.empty());

        ProcesarLiquidacionDTO dto = new ProcesarLiquidacionDTO();

        assertThrows(RecursoNoEncontradoException.class,
                () -> liquidacionService.procesarCierreLiquidacion(99L, dto));
    }

    /**
     * Valida la consulta y transformacion del historial de liquidaciones de un espacio.
     */
    @Test
    void obtenerHistorial_RetornaListaLiquidaciones() {
        Liquidacion liquidacion = Liquidacion.builder()
                .id(100L)
                .espacio(espacio)
                .montoTotal(BigDecimal.valueOf(50000.00))
                .fechaLiquidacion(LocalDate.of(2026, 9, 8))
                .mes(9)
                .anio(2026)
                .gastos(List.of(gasto1, gasto2))
                .build();

        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(liquidacionRepository.findByEspacio_IdOrderByFechaLiquidacionDesc(1L))
                .thenReturn(List.of(liquidacion));

        List<LiquidacionResponseDTO> historial = liquidacionService.obtenerHistorial(1L, null, null, null, null);

        assertNotNull(historial);
        assertEquals(1, historial.size());
        assertEquals(100L, historial.get(0).getId());
        assertEquals(BigDecimal.valueOf(50000.00), historial.get(0).getMontoTotal());
        assertEquals(2, historial.get(0).getCantidadGastos());
    }
}
