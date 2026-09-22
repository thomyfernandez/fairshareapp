package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.enums.ReglaDivision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida con el detalle de una plantilla de gasto recurrente (favorito), incluyendo
 * los campos calculados que no son editables por el cliente (id, nombreServicio, vencido).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaGastoResponseDTO {

    private Long id;
    private String nombre;
    private Integer frecuenciaAjusteMeses;
    private BigDecimal montoBase;
    private BigDecimal montoVariable;
    private LocalDate fechaProximaRevision;
    private Long espacioId;
    private Long servicioId;
    private String nombreServicio;
    private Long pagadorId;
    private Long categoriaId;
    private ReglaDivision reglaDivision;
    private Boolean vencido;
}
