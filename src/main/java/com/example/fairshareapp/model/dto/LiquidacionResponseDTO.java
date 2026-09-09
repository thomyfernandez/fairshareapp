package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Objeto de transferencia de datos con el detalle completo de una liquidacion de periodo:
 * las transferencias minimas a realizar y el resumen de gasto/presupuesto de cada usuario.
 * El id viene nulo cuando se trata de una previsualizacion sobre un periodo aun no cerrado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionResponseDTO {

    private Long id;
    private Long periodoId;
    private Long espacioId;
    private Integer anio;
    private Integer mes;
    private LocalDate fechaGeneracion;
    private BigDecimal montoTotalGastos;
    private List<LiquidacionTransferenciaDTO> transferencias;
    private List<ResumenUsuarioLiquidacionDTO> resumenPorUsuario;
}
