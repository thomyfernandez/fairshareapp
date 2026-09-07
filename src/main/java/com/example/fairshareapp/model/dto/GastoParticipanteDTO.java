package com.example.fairshareapp.model.dto;

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
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private Double importe;
    private Double porcentaje;
    private Double sueldo;
}
