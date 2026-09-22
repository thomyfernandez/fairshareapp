package com.example.fairshareapp.exception;

/**
 * Excepcion lanzada cuando ya existe un sueldo registrado para la misma
 * combinacion de usuario, anio y mes, evitando registros duplicados por periodo.
 */
public class SueldoDuplicadoException extends RuntimeException {

    /**
     * Constructor con mensaje descriptivo de la colision de periodo.
     *
     * @param mensaje Detalle del conflicto generado.
     */
    public SueldoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
