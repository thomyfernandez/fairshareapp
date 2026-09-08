package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.entity.ReglaDivision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Objeto de transferencia de datos para la solicitud de creacion de un gasto.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearGastoDTO {

    @NotBlank(message = "La descripcion del gasto es obligatoria")
    @Size(max = 255, message = "La descripcion no puede superar los 255 caracteres")
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto del gasto debe ser un valor positivo")
    private Double monto;

    private LocalDate fecha;

    @NotNull(message = "El identificador del usuario pagador es obligatorio")
    private Long pagadorId;

    private Long categoriaId;

    private ReglaDivision regla;

    @NotEmpty(message = "Debe incluir al menos un participante para la division del gasto")
    @Valid
    private List<GastoParticipanteDTO> participantes;
}
