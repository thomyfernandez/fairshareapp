package com.example.fairshareapp.exception;

/**
 * Excepcion generica para datos de entrada invalidos o incompletos en cualquier
 * operacion de negocio (monto invalido, datos de usuario invalidos, email duplicado, etc).
 * El manejo HTTP se centraliza en GlobalExceptionHandler.
 */
public class ArgumentInvalidException extends RuntimeException {

    public ArgumentInvalidException(String mensaje) {
        super(mensaje);
    }
}
