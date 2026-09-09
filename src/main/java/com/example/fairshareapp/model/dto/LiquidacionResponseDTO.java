package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa la respuesta estructurada de una liquidacion formal de un espacio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionResponseDTO {

    private Long id;
    private Long espacioId;
    private String espacioNombre;
    private BigDecimal montoTotal;
    private LocalDate fechaLiquidacion;
    private Integer mes;
    private Integer anio;
    private String descripcion;
    private int cantidadGastos;
    private List<Long> gastosLiquidadosIds;
    private ResumenCierreDTO resumenCierre;
}
