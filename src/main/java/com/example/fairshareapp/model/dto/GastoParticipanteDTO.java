package com.example.fairshareapp.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de transferencia de datos que representa a un participante de un gasto,
 * utilizado tanto para registrar particiones como para devolver su detalle.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoParticipanteDTO {

    private Long id;

    @NotNull(message = "El identificador del usuario participante es obligatorio")
    private Long usuarioId;

    private String usuarioNombre;

    private String usuarioEmail;

    @DecimalMin(value = "0.0", inclusive = true, message = "El importe no puede ser negativo")
    private Double importe;

    @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje no puede ser negativo")
    @DecimalMax(value = "100.0", inclusive = true, message = "El porcentaje no puede superar 100")
    private Double porcentaje;

    @DecimalMin(value = "0.0", inclusive = true, message = "El sueldo no puede ser negativo")
    private Double sueldo;
}
