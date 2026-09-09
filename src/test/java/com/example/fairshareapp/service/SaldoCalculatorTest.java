package com.example.fairshareapp.service;

import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.GastoParticipante;
import com.example.fairshareapp.model.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de logica pura para SaldoCalculator: el calculo del saldo neto por usuario
 * y su simplificacion en la minima cantidad de transferencias posible.
 */
class SaldoCalculatorTest {

    private final SaldoCalculator saldoCalculator = new SaldoCalculator();

    private Usuario juan;
    private Usuario maria;
    private Usuario pedro;

    /**
     * Configuracion inicial previa a cada prueba.
     */
    @BeforeEach
    void setUp() {
        juan = Usuario.builder().id(1L).nombre("Juan").apellido("Perez").build();
        maria = Usuario.builder().id(2L).nombre("Maria").apellido("Gomez").build();
        pedro = Usuario.builder().id(3L).nombre("Pedro").apellido("Diaz").build();
    }

    /**
     * Construye un gasto de prueba dividido en partes iguales entre los usuarios indicados.
     */
    private Gasto crearGastoEquitativo(Usuario pagador, double monto, Usuario... participantes) {
        Gasto gasto = Gasto.builder()
                .descripcion("Gasto de prueba")
                .monto(BigDecimal.valueOf(monto))
                .fecha(LocalDate.now())
                .pagador(pagador)
                .participantes(new ArrayList<>())
                .build();

        BigDecimal cuota = BigDecimal.valueOf(monto).divide(BigDecimal.valueOf(participantes.length), 2, java.math.RoundingMode.HALF_UP);
        for (Usuario usuario : participantes) {
            gasto.agregarParticipante(GastoParticipante.builder()
                    .usuario(usuario)
                    .importe(cuota)
                    .build());
        }
        return gasto;
    }

    @Test
    void calcularSaldosNetos_gastoEquitativoEntreDos_calculaSaldoOpuesto() {
        Gasto gasto = crearGastoEquitativo(juan, 1000.0, juan, maria);

        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        Map<Long, BigDecimal> saldos = saldoCalculator.calcularSaldosNetos(List.of(gasto), usuariosPorId);

        assertEquals(0, BigDecimal.valueOf(500.0).compareTo(saldos.get(juan.getId())));
        assertEquals(0, BigDecimal.valueOf(-500.0).compareTo(saldos.get(maria.getId())));
        assertEquals(2, usuariosPorId.size());
    }

    @Test
    void simplificarTransacciones_unPagadorUnDeudor_generaUnaSolaTransferencia() {
        Gasto gasto = crearGastoEquitativo(juan, 1000.0, juan, maria);
        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        Map<Long, BigDecimal> saldos = saldoCalculator.calcularSaldosNetos(List.of(gasto), usuariosPorId);

        List<TransferenciaCalculada> transferencias = saldoCalculator.simplificarTransacciones(saldos);

        assertEquals(1, transferencias.size());
        TransferenciaCalculada transferencia = transferencias.get(0);
        assertEquals(maria.getId(), transferencia.deudorId());
        assertEquals(juan.getId(), transferencia.acreedorId());
        assertEquals(0, BigDecimal.valueOf(500.0).compareTo(transferencia.monto()));
    }

    @Test
    void simplificarTransacciones_saldoYaBalanceado_noGeneraTransferencias() {
        // Juan paga 900 dividido entre los 3 (300 c/u) y Maria paga 300 dividido entre los 3 (100 c/u):
        // saldo neto Juan +600, Maria +100-100=0, Pedro -300-100=-400... se simplifica en una unica transferencia.
        Gasto gastoJuan = crearGastoEquitativo(juan, 900.0, juan, maria, pedro);
        Gasto gastoMaria = crearGastoEquitativo(maria, 300.0, juan, maria, pedro);

        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        Map<Long, BigDecimal> saldos = saldoCalculator.calcularSaldosNetos(List.of(gastoJuan, gastoMaria), usuariosPorId);
        List<TransferenciaCalculada> transferencias = saldoCalculator.simplificarTransacciones(saldos);

        BigDecimal totalTransferido = transferencias.stream()
                .map(TransferenciaCalculada::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, BigDecimal.valueOf(500.0).setScale(2).compareTo(totalTransferido.setScale(2)));
    }

    @Test
    void calcularSaldosNetos_sinGastos_devuelveMapaVacio() {
        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        Map<Long, BigDecimal> saldos = saldoCalculator.calcularSaldosNetos(List.of(), usuariosPorId);

        assertTrue(saldos.isEmpty());
        assertTrue(usuariosPorId.isEmpty());
    }

    @Test
    void simplificarTransacciones_saldosDentroDeTolerancia_noGeneraTransferencias() {
        Map<Long, BigDecimal> saldos = new HashMap<>();
        saldos.put(juan.getId(), BigDecimal.valueOf(0.005));
        saldos.put(maria.getId(), BigDecimal.valueOf(-0.005));

        List<TransferenciaCalculada> transferencias = saldoCalculator.simplificarTransacciones(saldos);

        assertTrue(transferencias.isEmpty());
    }
}
