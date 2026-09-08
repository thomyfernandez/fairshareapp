package com.example.fairshareapp.model.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ServicioVencimientoDTO {
    private Long servicioId;
    private String nombreServicio;
    private LocalDate fechaVencimiento;
}