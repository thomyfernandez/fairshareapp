package com.example.fairshareapp.model.entity;

/**
 * Estado de una deuda registrada entre un deudor y un acreedor dentro de un espacio.
 */
public enum EstadoDeuda {
    /**
     * La deuda tiene un saldo pendiente de pago mayor a cero.
     */
    PENDIENTE,

    /**
     * La deuda fue saldada en su totalidad.
     */
    SALDADO
}
