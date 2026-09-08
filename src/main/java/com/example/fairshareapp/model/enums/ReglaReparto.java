package com.example.fairshareapp.model.enums;

/**
 * Enumeracion que define las reglas de reparto y distribucion por defecto de un espacio.
 * Permite alternar entre division equitativa (50/50) y division proporcional a los ingresos/sueldos.
 */
public enum ReglaReparto {

    /**
     * Distribucion igualitaria o en partes iguales (50/50 entre dos miembros o partes identicas).
     */
    CINCUENTA_CINCUENTA,

    /**
     * Distribucion ponderada segun los ingresos o sueldos registrados de cada miembro.
     */
    PROPORCIONAL
}
