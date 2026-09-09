package com.example.fairshareapp.model.enums;

/**
 * Enumeracion que define las reglas de division aplicables a un gasto compartido.
 */
public enum ReglaDivision {
    /**
     * El gasto se divide en partes identicas entre todos los participantes.
     */
    EQUITATIVA,

    /**
     * Cada integrante aporta segun el sueldo mensual que declaro,
     * garantizando que a todos les quede el mismo porcentaje de sueldo libre para ahorrar.
     */
    PROPORCIONAL_INGRESOS,

    /**
     * Division en partes iguales pero eligiendo solo a ciertos miembros del grupo.
     */
    PARTICIPACION_PARCIAL,

    /**
     * Permite ingresar a mano porcentajes especificos (deben sumar 100%) o montos fijos exactos por persona.
     */
    PERSONALIZADA
}
