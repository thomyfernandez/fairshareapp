package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.GastoParticipante;
import com.example.fairshareapp.model.entity.Liquidacion;
import com.example.fairshareapp.model.entity.Periodo;
import com.example.fairshareapp.model.entity.Presupuesto;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.EstadoPeriodo;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.LiquidacionRepository;
import com.example.fairshareapp.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para verificar la logica de negocio de LiquidacionService:
 * el calculo de transferencias minimas de un periodo y la deteccion de excedente de presupuesto.
 */
@ExtendWith(MockitoExtension.class)
class LiquidacionServiceTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Spy
    private SaldoCalculator saldoCalculator = new SaldoCalculator();

    @InjectMocks
    private LiquidacionService liquidacionService;

    private final AtomicLong secuenciaId = new AtomicLong(1);
    private Espacio espacio;
    private Usuario juan;
    private Usuario maria;
    private Periodo periodo;

    @BeforeEach
    void setUp() {
        espacio = Espacio.builder().id(1L).nombre("Depto Palermo").build();
        juan = Usuario.builder().id(1L).nombre("Juan").apellido("Perez").build();
        maria = Usuario.builder().id(2L).nombre("Maria").apellido("Gomez").build();
        periodo = Periodo.builder().id(10L).espacio(espacio).anio(2026).mes(9).estado(EstadoPeriodo.CERRADO).build();

        org.mockito.Mockito.lenient().when(liquidacionRepository.save(any(Liquidacion.class))).thenAnswer(inv -> {
            Liquidacion liquidacion = inv.getArgument(0);
            liquidacion.setId(secuenciaId.getAndIncrement());
            return liquidacion;
        });
    }

    /**
     * Construye un gasto de prueba dividido en partes iguales entre los usuarios indicados.
     */
    private Gasto crearGastoEquitativo(Usuario pagador, double monto, Usuario... participantes) {
        Gasto gasto = Gasto.builder()
                .descripcion("Gasto de prueba")
                .monto(BigDecimal.valueOf(monto))
                .fecha(LocalDate.of(2026, 9, 10))
                .espacio(espacio)
                .pagador(pagador)
                .participantes(new ArrayList<>())
                .build();

        BigDecimal cuota = BigDecimal.valueOf(monto).divide(BigDecimal.valueOf(participantes.length), 2, java.math.RoundingMode.HALF_UP);
        for (Usuario usuario : participantes) {
            gasto.agregarParticipante(GastoParticipante.builder().usuario(usuario).importe(cuota).build());
        }
        return gasto;
    }

    @Test
    void generarYPersistir_gastoEquitativoEntreDos_calculaTransferenciaEsperada() {
        Gasto gasto = crearGastoEquitativo(juan, 1000.0, juan, maria);

        when(liquidacionRepository.existsByPeriodoId(10L)).thenReturn(false);
        when(gastoRepository.findByEspacioIdAndFechaBetweenOrderByFechaDesc(1L, periodo.getFechaInicio(), periodo.getFechaFin()))
                .thenReturn(List.of(gasto));
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 1L, 2026, 9)).thenReturn(Optional.empty());
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 2L, 2026, 9)).thenReturn(Optional.empty());

        LiquidacionResponseDTO resultado = liquidacionService.generarYPersistir(periodo);

        assertEquals(1, resultado.getTransferencias().size());
        assertEquals(maria.getId(), resultado.getTransferencias().get(0).getDeudorId());
        assertEquals(juan.getId(), resultado.getTransferencias().get(0).getAcreedorId());
        assertEquals(0, BigDecimal.valueOf(500.0).compareTo(resultado.getTransferencias().get(0).getMonto()));
        assertEquals(0, BigDecimal.valueOf(1000.0).compareTo(resultado.getMontoTotalGastos()));
        assertEquals(2, resultado.getResumenPorUsuario().size());
    }

    @Test
    void generarYPersistir_usuarioSuperaPresupuesto_marcaExcedido() {
        Gasto gasto = crearGastoEquitativo(juan, 1000.0, juan, maria);
        Presupuesto presupuestoMaria = Presupuesto.builder().id(20L).espacio(espacio).usuario(maria)
                .anio(2026).mes(9).montoLimite(BigDecimal.valueOf(300.0)).build();

        when(liquidacionRepository.existsByPeriodoId(10L)).thenReturn(false);
        when(gastoRepository.findByEspacioIdAndFechaBetweenOrderByFechaDesc(1L, periodo.getFechaInicio(), periodo.getFechaFin()))
                .thenReturn(List.of(gasto));
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 1L, 2026, 9)).thenReturn(Optional.empty());
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 2L, 2026, 9)).thenReturn(Optional.of(presupuestoMaria));

        LiquidacionResponseDTO resultado = liquidacionService.generarYPersistir(periodo);

        var resumenMaria = resultado.getResumenPorUsuario().stream()
                .filter(r -> r.getUsuarioId().equals(maria.getId()))
                .findFirst().orElseThrow();

        assertTrue(resumenMaria.getPresupuestoExcedido());
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(resumenMaria.getMontoExcedente()));

        var resumenJuan = resultado.getResumenPorUsuario().stream()
                .filter(r -> r.getUsuarioId().equals(juan.getId()))
                .findFirst().orElseThrow();
        assertEquals(null, resumenJuan.getPresupuestoExcedido());
    }

    @Test
    void generarYPersistir_usuarioDentroDePresupuesto_noMarcaExcedido() {
        Gasto gasto = crearGastoEquitativo(juan, 1000.0, juan, maria);
        Presupuesto presupuestoMaria = Presupuesto.builder().id(20L).espacio(espacio).usuario(maria)
                .anio(2026).mes(9).montoLimite(BigDecimal.valueOf(1000.0)).build();

        when(liquidacionRepository.existsByPeriodoId(10L)).thenReturn(false);
        when(gastoRepository.findByEspacioIdAndFechaBetweenOrderByFechaDesc(1L, periodo.getFechaInicio(), periodo.getFechaFin()))
                .thenReturn(List.of(gasto));
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 1L, 2026, 9)).thenReturn(Optional.empty());
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 2L, 2026, 9)).thenReturn(Optional.of(presupuestoMaria));

        LiquidacionResponseDTO resultado = liquidacionService.generarYPersistir(periodo);

        var resumenMaria = resultado.getResumenPorUsuario().stream()
                .filter(r -> r.getUsuarioId().equals(maria.getId()))
                .findFirst().orElseThrow();

        assertEquals(false, resumenMaria.getPresupuestoExcedido());
        assertEquals(0, BigDecimal.ZERO.compareTo(resumenMaria.getMontoExcedente()));
    }

    @Test
    void generarYPersistir_periodoYaLiquidado_lanzaExcepcion() {
        when(liquidacionRepository.existsByPeriodoId(10L)).thenReturn(true);

        assertThrows(ReglaInvalidaException.class, () -> liquidacionService.generarYPersistir(periodo));
    }

    @Test
    void obtenerPorId_liquidacionInexistente_lanzaExcepcion() {
        when(liquidacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(com.example.fairshareapp.exception.RecursoNoEncontradoException.class,
                () -> liquidacionService.obtenerPorId(99L));
    }
}
