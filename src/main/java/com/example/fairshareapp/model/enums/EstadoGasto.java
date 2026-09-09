package com.example.fairshareapp.model.enums;

/**
 * Enumeracion que representa los estados posibles de un gasto dentro del ciclo de vida de liquidacion.
 */
public enum EstadoGasto {
    /**
     * El gasto ha sido registrado pero aun no ha sido incluido en una liquidacion cerrada.
     */
    PENDIENTE,

    /**
     * El gasto ha sido procesado y cerrado dentro de una liquidacion formal del espacio.
     */
    LIQUIDADO
}
