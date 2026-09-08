package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.entity.ReglaDivision;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la creacion, consulta y actualizacion de plantillas de gastos recurrentes (favoritos).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaGastoDTO {

    private Long id;

    @NotBlank(message = "El nombre de la plantilla es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Min(value = 1, message = "La frecuencia de ajuste debe ser de al menos 1 mes")
    private Integer frecuenciaAjusteMeses;

    @NotNull(message = "El monto base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El monto base no puede ser negativo")
    private BigDecimal montoBase;

    @DecimalMin(value = "0.0", inclusive = true, message = "El monto variable no puede ser negativo")
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