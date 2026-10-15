package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.enums.ReglaDivision;
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
 * DTO de entrada para la creacion de una plantilla de gasto recurrente (favorito).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaGastoRequestDTO {

    @NotBlank(message = "El nombre de la plantilla es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Min(value = 1, message = "La frecuencia de ajuste debe ser de al menos 1 mes")
    private Integer frecuenciaAjusteMeses;

    @NotNull(message = "El monto base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El monto base no puede ser negativo")
    @jakarta.validation.constraints.Digits(integer=10, fraction=2, message="Máximo 10 enteros y 2 decimales")
    private BigDecimal montoBase;

    @DecimalMin(value = "0.0", inclusive = true, message = "El monto variable no puede ser negativo")
    @jakarta.validation.constraints.Digits(integer=10, fraction=2, message="Máximo 10 enteros y 2 decimales")
    private BigDecimal montoVariable;

    @NotNull(message = "La fecha de proxima revision del ciclo es obligatoria")
    private LocalDate fechaProximaRevision;

    /**
     * Identificador del espacio. Es opcional: cuando la solicitud llega junto con el espacioId de la ruta,
     * el controller exige que ambos coincidan.
     */
    private Long espacioId;

    private Long servicioId;

    @NotNull(message = "El identificador del pagador es obligatorio")
    private Long pagadorId;

    private Long categoriaId;

    private ReglaDivision reglaDivision;
}
