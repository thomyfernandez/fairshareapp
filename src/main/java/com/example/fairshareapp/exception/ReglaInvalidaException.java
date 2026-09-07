package com.example.fairshareapp.exception;

/**
 * Excepcion lanzada cuando los datos de division de un gasto no cumplen con la regla de negocio activa.
 */
public class ReglaInvalidaException extends RuntimeException {

    /**
     * Constructor con mensaje explicativo de la inconsistencia en la regla de division.
     *
     * @param mensaje Mensaje explicativo del error de validacion de la regla.
     */
    public ReglaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
