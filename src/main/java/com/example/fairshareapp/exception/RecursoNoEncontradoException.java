package com.example.fairshareapp.exception;

/**
 * Excepcion lanzada cuando no se encuentra un recurso solicitado en la base de datos.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Constructor con mensaje descriptivo del recurso faltante.
     *
     * @param mensaje Mensaje explicativo del recurso no encontrado.
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
