package com.example.fairshareapp.exception;

/**
 * Excepcion lanzada cuando un usuario autenticado no cuenta con los permisos necesarios
 * para ejecutar la operacion solicitada sobre el recurso (HTTP 403 Forbidden).
 * Categoria publicada tempranamente para que los servicios que validan permisos
 * (por ejemplo, administracion de espacios o miembros) la reutilicen en lugar de
 * seguir modelando la falta de permisos como una regla de negocio generica (400).
 */
public class AccesoDenegadoException extends RuntimeException {

    /**
     * Construye la excepcion con un mensaje descriptivo del permiso faltante.
     *
     * @param mensaje Detalle del motivo de la denegacion de acceso.
     */
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
