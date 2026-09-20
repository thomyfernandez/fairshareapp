package com.example.fairshareapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepcion lanzada cuando se intenta registrar a un usuario en un espacio
 * compartido al cual ya pertenece previamente, evitando membresias duplicadas.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class MembresiaDuplicadaException extends RuntimeException {

    /**
     * Construye la excepcion con un mensaje descriptivo de la colision de membresia.
     *
     * @param message Detalle del conflicto generado.
     */
    public MembresiaDuplicadaException(String message) {
        super(message);
    }
}
