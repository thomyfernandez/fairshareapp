package com.example.fairshareapp.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la actualizacion de montos de un ciclo de gasto recurrente por ajuste o inflacion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPrecioCicloDTO {

    @NotNull(message = "El nuevo monto base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo monto base debe ser mayor o igual a cero")
    private BigDecimal nuevoMontoBase;

    @DecimalMin(value = "0.0", inclusive = true, message = "El nuevo monto variable no puede ser negativo")
    private BigDecimal nuevoMontoVariable;

    private LocalDate nuevaFechaProximaRevision;
}