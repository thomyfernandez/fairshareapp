package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos con el detalle de una transferencia minima entre dos usuarios,
 * calculada como parte de una liquidacion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionTransferenciaDTO {

    private Long deudorId;
    private String deudorNombre;
    private Long acreedorId;
    private String acreedorNombre;
    private BigDecimal monto;
}
