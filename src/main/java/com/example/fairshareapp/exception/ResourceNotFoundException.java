package com.example.fairshareapp.exception;

/**
 * Excepcion generica para cualquier recurso que no se encuentra en la base de datos
 * (Gasto, Usuario, Pedido, Producto, etc). El manejo HTTP se centraliza en GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id: " + id);
    }

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
