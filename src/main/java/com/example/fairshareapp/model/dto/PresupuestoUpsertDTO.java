package com.example.fairshareapp.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos para fijar el presupuesto mensual de un usuario en un espacio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoUpsertDTO {

    @NotNull(message = "El monto limite del presupuesto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto limite debe ser un valor positivo")
    private BigDecimal montoLimite;
}
