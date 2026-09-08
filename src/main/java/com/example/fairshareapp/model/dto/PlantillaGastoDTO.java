package com.example.fairshareapp.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PlantillaGastoDTO {
    private Long id;
    private String nombre;
    private Integer frecuenciaAjusteMeses;
    private BigDecimal montoBase;
    private BigDecimal montoVariable;
    private LocalDate fechaProximaRevision;
    private Long espacioId;
}