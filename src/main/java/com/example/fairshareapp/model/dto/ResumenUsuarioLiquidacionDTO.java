package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos con el resumen de gasto y presupuesto de un usuario
 * dentro de una liquidacion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenUsuarioLiquidacionDTO {

    private Long usuarioId;
    private String usuarioNombre;
    private BigDecimal totalGastado;
    private BigDecimal presupuestoLimite;
    private Boolean presupuestoExcedido;
    private BigDecimal montoExcedente;
}
