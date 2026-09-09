package com.example.fairshareapp.model.enums;

/**
 * Enumeracion que representa el estado de un periodo mensual de un espacio compartido.
 */
public enum EstadoPeriodo {

    /**
     * El periodo admite el registro de nuevos gastos.
     */
    ABIERTO,

    /**
     * El periodo fue cerrado: no admite nuevos gastos y ya cuenta con su liquidacion generada.
     */
    CERRADO
}
