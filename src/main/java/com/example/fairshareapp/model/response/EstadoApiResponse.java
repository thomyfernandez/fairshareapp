package com.example.fairshareapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de transferencia de datos con el estado de salud del servicio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoApiResponse {

    private String status;
    private String service;
    private long timestamp;
}
