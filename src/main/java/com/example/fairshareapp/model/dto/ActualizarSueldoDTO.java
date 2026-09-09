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
 * DTO para registrar o actualizar el sueldo mensual declarado de un miembro dentro de un espacio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarSueldoDTO {

    @NotNull(message = "El sueldo declarado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El sueldo declarado no puede ser negativo")
    private BigDecimal sueldoDeclarado;
}
