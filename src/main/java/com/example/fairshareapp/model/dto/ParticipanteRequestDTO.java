package com.example.fairshareapp.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParticipanteRequestDTO {

    @NotNull(message = "El ID del participante es obligatorio")
    private Long usuarioId;


    @NotNull(message = "El valor de participación es obligatorio")
    @PositiveOrZero(message = "El valor de participación no puede ser negativo")
    private BigDecimal valorParticipacion; 
}