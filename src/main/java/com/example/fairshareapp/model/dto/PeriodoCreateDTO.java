package com.example.fairshareapp.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de transferencia de datos para la solicitud de apertura de un periodo mensual.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoCreateDTO {

    @NotNull(message = "El anio del periodo es obligatorio")
    @Min(value = 2000, message = "El anio del periodo debe ser valido")
    private Integer anio;

    @NotNull(message = "El mes del periodo es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    private Integer mes;
}
