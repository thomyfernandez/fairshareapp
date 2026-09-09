package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos con el detalle del presupuesto mensual de un usuario en un espacio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoResponseDTO {

    private Long id;
    private Long espacioId;
    private Long usuarioId;
    private String usuarioNombre;
    private Integer anio;
    private Integer mes;
    private BigDecimal montoLimite;
}
