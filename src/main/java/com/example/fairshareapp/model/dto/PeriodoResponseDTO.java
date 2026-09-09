package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.enums.EstadoPeriodo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Objeto de transferencia de datos con el detalle de un periodo mensual de un espacio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoResponseDTO {

    private Long id;
    private Long espacioId;
    private Integer anio;
    private Integer mes;
    private EstadoPeriodo estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaCierre;
    private Long liquidacionId;
}
