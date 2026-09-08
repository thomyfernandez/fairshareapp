package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de transferencia de datos para registrar el pago de una deuda.
 * Si no se especifica un monto, se interpreta como la cancelacion total del saldo pendiente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarPagoDTO {

    private Double monto;
    private Long liquidacionId;
}
