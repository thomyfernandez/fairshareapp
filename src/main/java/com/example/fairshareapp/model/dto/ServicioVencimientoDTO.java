package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que alerta sobre el vencimiento del ciclo tarifario de un servicio o plantilla de gasto recurrente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioVencimientoDTO {

    private Long servicioId;
    private String nombreServicio;
    private LocalDate fechaVencimiento;
    private BigDecimal montoBase;
    private BigDecimal montoVariable;
    private Long diasVencido;
}