package com.example.fairshareapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para interceptar y estandarizar las respuestas de error en la API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones cuando un correo electronico ya se encuentra registrado en el sistema.
     *
     * @param ex Excepcion capturada.
     * @return Respuesta con codigo HTTP 409 Conflict.
     */
    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<String> handleEmailYaRegistrado(EmailYaRegistradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Maneja excepciones cuando las credenciales de autenticacion son invalidas.
     *
     * @param ex Excepcion capturada.
     * @return Respuesta con codigo HTTP 401 Unauthorized.
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<String> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Maneja excepciones cuando un recurso no es encontrado en la base de datos.
     *
     * @param ex Excepcion capturada.
     * @return Respuesta con codigo HTTP 404 Not Found.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Maneja excepciones provocadas por inconsistencias o violaciones de reglas de division de gastos.
     *
     * @param ex Excepcion capturada.
     * @return Respuesta con codigo HTTP 400 Bad Request.
     */
    @ExceptionHandler(ReglaInvalidaException.class)
    public ResponseEntity<String> handleReglaInvalida(ReglaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Maneja argumentos ilegales o parametros invalidos enviados a la API.
     *
     * @param ex Excepcion capturada.
     * @return Respuesta con codigo HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Maneja cualquier excepcion generica no prevista en los controladores.
     *
     * @param ex Excepcion capturada.
     * @return Respuesta con codigo HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrio un error inesperado");
    }
}