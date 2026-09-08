package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Objeto de transferencia de datos con la matriz simplificada de deudas pendientes de un espacio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDTO {

    private Long espacioId;
    private String espacioNombre;
    private List<DeudaDetalleDTO> deudas;
}
