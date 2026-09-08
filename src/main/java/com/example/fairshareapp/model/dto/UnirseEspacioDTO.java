package com.example.fairshareapp.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para unirse a un espacio compartido mediante su codigo de invitacion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnirseEspacioDTO {

    @NotBlank(message = "El codigo del espacio es obligatorio")
    private String codigo;

    @NotNull(message = "El identificador del usuario es obligatorio")
    private Long usuarioId;

    @DecimalMin(value = "0.0", inclusive = true, message = "El sueldo declarado no puede ser negativo")
    private BigDecimal sueldoDeclarado;
}
