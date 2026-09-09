package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que resume el impacto presupuestario y cuantitativo del cierre de una liquidacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCierreDTO {

    /**
     * Importe total descontado del presupuesto del espacio.
     */
    private BigDecimal totalLiquidado;

    /**
     * Presupuesto base que tenia el espacio antes de procesar el cierre.
     */
    private BigDecimal presupuestoAnterior;

    /**
     * Presupuesto base remanente del espacio tras el descuento del total liquidado.
     */
    private BigDecimal presupuestoRestante;

    /**
     * Cantidad total de gastos que pasaron a estado LIQUIDADO.
     */
    private int cantidadGastosCerrados;

    /**
     * Fecha en que se efectivizo el corte o cierre.
     */
    private LocalDate fechaCierre;
}
