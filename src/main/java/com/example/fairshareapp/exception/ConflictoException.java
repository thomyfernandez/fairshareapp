package com.example.fairshareapp.exception;

/**
 * Categoria general para conflictos de estado del recurso (HTTP 409), como violaciones
 * de unicidad o duplicados logicos de negocio. Las excepciones de conflicto especificas
 * (por ejemplo EmailYaRegistradoException o MembresiaDuplicadaException) extienden esta
 * clase para ser reconocidas automaticamente por GlobalExceptionHandler sin necesidad de
 * registrar un @ExceptionHandler adicional por cada una.
 */
public class ConflictoException extends RuntimeException {

    /**
     * Construye la excepcion con un mensaje descriptivo del conflicto detectado.
     *
     * @param mensaje Detalle del conflicto.
     */
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
