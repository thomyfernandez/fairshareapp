package com.example.fairshareapp.service;

import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.GastoParticipante;
import com.example.fairshareapp.model.entity.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Componente de logica pura que calcula el saldo neto de un conjunto de usuarios a partir
 * de sus gastos compartidos y simplifica ese resultado en la minima cantidad de transferencias.
 * Extraido de BalanceService para poder ser reutilizado por el motor de liquidacion de periodos.
 */
@Component
public class SaldoCalculator {

    private static final BigDecimal TOLERANCIA = BigDecimal.valueOf(0.01);

    /**
     * Calcula el saldo neto de cada usuario dentro de un espacio: positivo si le deben plata,
     * negativo si el debe. Recorre cada gasto sumando al pagador lo que adelanto y restando
     * a cada participante la cuota que le correspondia cubrir.
     *
     * @param gastos Lista de gastos a consolidar.
     * @param usuariosPorId Mapa de salida que se completa con los usuarios involucrados, para evitar consultas adicionales.
     * @return Mapa de identificador de usuario a su saldo neto.
     */
    public Map<Long, BigDecimal> calcularSaldosNetos(List<Gasto> gastos, Map<Long, Usuario> usuariosPorId) {
        Map<Long, BigDecimal> saldosNetos = new HashMap<>();

        for (Gasto gasto : gastos) {
            Usuario pagador = gasto.getPagador();
            usuariosPorId.put(pagador.getId(), pagador);

            for (GastoParticipante participante : gasto.getParticipantes()) {
                Usuario usuario = participante.getUsuario();
                usuariosPorId.put(usuario.getId(), usuario);

                BigDecimal importe = participante.getImporte();
                saldosNetos.merge(usuario.getId(), importe.negate(), BigDecimal::add);
                saldosNetos.merge(pagador.getId(), importe, BigDecimal::add);
            }
        }

        return saldosNetos;
    }

    /**
     * Simplifica los saldos netos en la minima cantidad de transferencias posible,
     * emparejando en cada paso al deudor y al acreedor con mayor magnitud pendiente.
     *
     * @param saldosNetos Mapa de identificador de usuario a su saldo neto.
     * @return Lista de transferencias simplificadas (quien le debe a quien y cuanto).
     */
    public List<TransferenciaCalculada> simplificarTransacciones(Map<Long, BigDecimal> saldosNetos) {
        List<SaldoPersona> deudores = new ArrayList<>();
        List<SaldoPersona> acreedores = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : saldosNetos.entrySet()) {
            BigDecimal saldo = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            if (saldo.compareTo(TOLERANCIA.negate()) < 0) {
                deudores.add(new SaldoPersona(entry.getKey(), saldo.negate()));
            } else if (saldo.compareTo(TOLERANCIA) > 0) {
                acreedores.add(new SaldoPersona(entry.getKey(), saldo));
            }
        }

        deudores.sort((a, b) -> b.monto.compareTo(a.monto));
        acreedores.sort((a, b) -> b.monto.compareTo(a.monto));

        List<TransferenciaCalculada> transferencias = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < deudores.size() && j < acreedores.size()) {
            SaldoPersona deudor = deudores.get(i);
            SaldoPersona acreedor = acreedores.get(j);
            BigDecimal monto = deudor.monto.min(acreedor.monto);

            if (monto.compareTo(TOLERANCIA) > 0) {
                transferencias.add(new TransferenciaCalculada(deudor.usuarioId, acreedor.usuarioId, monto));
            }

            deudor.monto = deudor.monto.subtract(monto);
            acreedor.monto = acreedor.monto.subtract(monto);

            if (deudor.monto.compareTo(TOLERANCIA) <= 0) {
                i++;
            }
            if (acreedor.monto.compareTo(TOLERANCIA) <= 0) {
                j++;
            }
        }

        return transferencias;
    }

    /**
     * Estructura mutable auxiliar para el algoritmo greedy de simplificacion,
     * que necesita ir reduciendo el monto pendiente de cada persona en cada iteracion.
     */
    private static final class SaldoPersona {
        private final Long usuarioId;
        private BigDecimal monto;

        private SaldoPersona(Long usuarioId, BigDecimal monto) {
            this.usuarioId = usuarioId;
            this.monto = monto;
        }
    }
}
