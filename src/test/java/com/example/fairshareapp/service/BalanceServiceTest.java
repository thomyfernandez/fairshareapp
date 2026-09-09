package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.BalanceDTO;
import com.example.fairshareapp.model.dto.DeudaDetalleDTO;
import com.example.fairshareapp.model.dto.RegistrarPagoDTO;
import com.example.fairshareapp.model.enums.EstadoDeuda;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.GastoParticipante;
import com.example.fairshareapp.model.enums.ReglaDivision;
import com.example.fairshareapp.model.entity.SaldoDeuda;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.SaldoDeudaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para verificar la logica de negocio de BalanceService:
 * el calculo del saldo neto por usuario, la simplificacion en transacciones minimas
 * y el registro de pagos sobre las deudas persistidas.
 */
@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private SaldoDeudaRepository saldoDeudaRepository;

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @InjectMocks
    private BalanceService balanceService;

    private Espacio espacio;
    private Usuario juan;
    private Usuario maria;
    private final AtomicLong secuenciaId = new AtomicLong(1);

    /**
     * Configuracion inicial previa a cada prueba.
     */
    @BeforeEach
    void setUp() {
        espacio = Espacio.builder().id(1L).nombre("Depto Palermo").build();
        juan = Usuario.builder().id(1L).nombre("Juan").apellido("Perez").build();
        maria = Usuario.builder().id(2L).nombre("Maria").apellido("Gomez").build();

        lenient().when(saldoDeudaRepository.save(any(SaldoDeuda.class))).thenAnswer(invocation -> {
            SaldoDeuda deuda = invocation.getArgument(0);
            if (deuda.getId() == null) {
                deuda.setId(secuenciaId.getAndIncrement());
            }
            return deuda;
        });
    }

    /**
     * Construye un gasto de prueba dividido en partes iguales entre los usuarios indicados.
     */
    private Gasto crearGastoEquitativo(Usuario pagador, double monto, Usuario... participantes) {
        BigDecimal montoBD = BigDecimal.valueOf(monto);
        Gasto gasto = Gasto.builder()
                .id(secuenciaId.getAndIncrement())
                .descripcion("Gasto de prueba")
                .monto(montoBD)
                .fecha(LocalDate.now())
                .espacio(espacio)
                .pagador(pagador)
                .reglaDivision(ReglaDivision.EQUITATIVA)
                .participantes(new ArrayList<>())
                .build();

        BigDecimal cuota = montoBD.divide(BigDecimal.valueOf(participantes.length), 2, RoundingMode.HALF_UP);
        for (Usuario usuario : participantes) {
            gasto.getParticipantes().add(GastoParticipante.builder()
                    .gasto(gasto)
                    .usuario(usuario)
                    .importe(cuota)
                    .build());
        }
        return gasto;
    }

    @Test
    void obtenerBalance_gastoEquitativoEntreDos_generaDeudaSimplificada() {
        Gasto gasto = crearGastoEquitativo(juan, 1000.0, juan, maria);

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(gastoRepository.findByEspacioIdOrderByFechaDesc(1L)).thenReturn(List.of(gasto));
        when(saldoDeudaRepository.findByEspacioIdAndEstado(1L, EstadoDeuda.PENDIENTE)).thenReturn(List.of());

        BalanceDTO balance = balanceService.obtenerBalance(1L);

        assertEquals(1, balance.getDeudas().size());
        DeudaDetalleDTO deuda = balance.getDeudas().get(0);
        assertEquals(maria.getId(), deuda.getDeudorId());
        assertEquals(juan.getId(), deuda.getAcreedorId());
        assertEquals(500.0, deuda.getMonto().doubleValue(), 0.01);
        assertEquals(EstadoDeuda.PENDIENTE, deuda.getEstado());
    }

    @Test
    void obtenerBalance_variosGastosCruzados_simplificaEnUnaSolaTransaccion() {
        Usuario pedro = Usuario.builder().id(3L).nombre("Pedro").apellido("Diaz").build();

        // Juan pago 900 dividido entre los 3 (300 c/u) y Maria pago 300 dividido entre los 3 (100 c/u).
        Gasto gastoJuan = crearGastoEquitativo(juan, 900.0, juan, maria, pedro);
        Gasto gastoMaria = crearGastoEquitativo(maria, 300.0, juan, maria, pedro);

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(gastoRepository.findByEspacioIdOrderByFechaDesc(1L)).thenReturn(List.of(gastoJuan, gastoMaria));
        when(saldoDeudaRepository.findByEspacioIdAndEstado(1L, EstadoDeuda.PENDIENTE)).thenReturn(List.of());

        BalanceDTO balance = balanceService.obtenerBalance(1L);

        // Saldos netos: Juan +500, Maria -100 y Pedro -400.
        double totalDeudas = 0.0;
        for (DeudaDetalleDTO deuda : balance.getDeudas()) {
            totalDeudas += deuda.getMonto().doubleValue();
        }
        assertEquals(500.0, totalDeudas, 0.01);
    }

    @Test
    void obtenerBalance_espacioInexistente_lanzaExcepcion() {
        when(espacioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> balanceService.obtenerBalance(99L));
    }

    @Test
    void registrarPago_sinMonto_saldaLaDeudaCompleta() {
        SaldoDeuda deuda = SaldoDeuda.builder()
                .id(10L)
                .espacio(espacio)
                .deudor(maria)
                .acreedor(juan)
                .montoOriginal(BigDecimal.valueOf(500.0))
                .monto(BigDecimal.valueOf(500.0))
                .estado(EstadoDeuda.PENDIENTE)
                .build();

        when(saldoDeudaRepository.findById(10L)).thenReturn(Optional.of(deuda));

        DeudaDetalleDTO resultado = balanceService.registrarPago(10L, RegistrarPagoDTO.builder().build());

        assertEquals(EstadoDeuda.SALDADO, resultado.getEstado());
        assertEquals(0.0, resultado.getMonto().doubleValue(), 0.01);
    }

    @Test
    void registrarPago_conMontoParcial_dejaSaldoPendiente() {
        SaldoDeuda deuda = SaldoDeuda.builder()
                .id(10L)
                .espacio(espacio)
                .deudor(maria)
                .acreedor(juan)
                .montoOriginal(BigDecimal.valueOf(500.0))
                .monto(BigDecimal.valueOf(500.0))
                .estado(EstadoDeuda.PENDIENTE)
                .build();

        when(saldoDeudaRepository.findById(10L)).thenReturn(Optional.of(deuda));

        DeudaDetalleDTO resultado = balanceService.registrarPago(10L, RegistrarPagoDTO.builder().monto(BigDecimal.valueOf(200.0)).build());

        assertEquals(EstadoDeuda.PENDIENTE, resultado.getEstado());
        assertEquals(300.0, resultado.getMonto().doubleValue(), 0.01);
    }

    @Test
    void registrarPago_deudaYaSaldada_lanzaExcepcion() {
        SaldoDeuda deuda = SaldoDeuda.builder()
                .id(10L)
                .espacio(espacio)
                .deudor(maria)
                .acreedor(juan)
                .montoOriginal(BigDecimal.valueOf(500.0))
                .monto(BigDecimal.ZERO)
                .estado(EstadoDeuda.SALDADO)
                .build();

        when(saldoDeudaRepository.findById(10L)).thenReturn(Optional.of(deuda));

        RegistrarPagoDTO pago = RegistrarPagoDTO.builder().build();
        assertThrows(ReglaInvalidaException.class,
                () -> balanceService.registrarPago(10L, pago));
    }

    @Test
    void registrarPago_montoSuperaElPendiente_lanzaExcepcion() {
        SaldoDeuda deuda = SaldoDeuda.builder()
                .id(10L)
                .espacio(espacio)
                .deudor(maria)
                .acreedor(juan)
                .montoOriginal(BigDecimal.valueOf(500.0))
                .monto(BigDecimal.valueOf(500.0))
                .estado(EstadoDeuda.PENDIENTE)
                .build();

        when(saldoDeudaRepository.findById(10L)).thenReturn(Optional.of(deuda));

        RegistrarPagoDTO pago = RegistrarPagoDTO.builder().monto(BigDecimal.valueOf(600.0)).build();
        assertThrows(ReglaInvalidaException.class,
                () -> balanceService.registrarPago(10L, pago));
    }
}
