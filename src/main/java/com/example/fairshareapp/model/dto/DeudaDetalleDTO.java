package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.entity.EstadoDeuda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de transferencia de datos con el detalle de una deuda entre dos usuarios de un espacio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeudaDetalleDTO {

    private Long id;
    private Long deudorId;
    private String deudorNombre;
    private Long acreedorId;
    private String acreedorNombre;
    private Double monto;
    private EstadoDeuda estado;
    private Long liquidacionId;
}
