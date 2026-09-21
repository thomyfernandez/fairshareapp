package com.example.fairshareapp.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class GastoRequestDTO {

    @NotNull(message = "El ID del espacio es obligatorio")
    private Long espacioId;

    @NotNull(message = "El ID del usuario pagador es obligatorio")
    private Long pagadorId;

    @NotBlank(message = "La descripción del gasto no puede estar vacía")
    private String descripcion;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor a cero")
    private BigDecimal montoTotal;

    @NotNull(message = "La fecha del gasto es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "La regla de división es obligatoria (ej: EQUITATIVA, PORCENTUAL)")
    private String reglaDivision;

    @NotEmpty(message = "La lista de participantes no puede estar vacía")
    @Valid // Valida campos anteriores
    private List<ParticipanteRequestDTO> participantes;
}