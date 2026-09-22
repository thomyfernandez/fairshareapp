package com.example.fairshareapp.exception;

import com.example.fairshareapp.model.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manejador global de excepciones: unica clase que traduce cualquier error de
 * la API
 * a un contrato de respuesta comun (ErrorResponseDTO), con el codigo HTTP y la
 * categoria
 * que correspondan. Ningun otro @RestControllerAdvice debe agregarse al
 * proyecto: toda
 * nueva categoria de error se resuelve agregando una excepcion reutilizable
 * aqui.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== Recurso inexistente (404) ====================

    /**
     * Maneja excepciones cuando un recurso no es encontrado en la base de datos.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleRecursoNoEncontrado(RecursoNoEncontradoException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage(), request);
    }

    // ==================== Validacion / regla de negocio (400) ====================

    /**
     * Maneja excepciones provocadas por inconsistencias o violaciones de reglas de
     * negocio.
     */
    @ExceptionHandler(ReglaInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> handleReglaInvalida(ReglaInvalidaException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "REGLA_INVALIDA", ex.getMessage(), request);
    }

    /**
     * Maneja argumentos ilegales o parametros invalidos enviados a la API.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "ARGUMENTO_INVALIDO", ex.getMessage(), request);
    }

    /**
     * Maneja errores de validacion de un DTO anotado con @Valid en el cuerpo de la
     * solicitud,
     * devolviendo todos los campos invalidos (nunca el valor rechazado, ya que
     * puede ser sensible).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidacionDTO(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(),
                    error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor invalido");
        }
        return construir(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene datos invalidos", request, fieldErrors);
    }

    /**
     * Maneja errores de validacion sobre parametros sueltos (query params o path
     * variables)
     * anotados directamente con restricciones de Bean Validation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String ruta = violation.getPropertyPath().toString();
            String campo = ruta.contains(".") ? ruta.substring(ruta.lastIndexOf('.') + 1) : ruta;
            fieldErrors.put(campo, violation.getMessage());
        }
        return construir(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene parametros invalidos", request, fieldErrors);
    }

    /**
     * Maneja un cuerpo de solicitud JSON mal formado, con tipos incorrectos o
     * valores de enum
     * que no corresponden a ninguna constante valida.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonMalFormado(HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "JSON_MALFORMADO",
                "El cuerpo de la solicitud contiene un formato JSON invalido o valores no reconocidos", request);
    }

    /**
     * Maneja la ausencia de un parametro obligatorio (query param o form param) en
     * la solicitud.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleParametroFaltante(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = Map.of(ex.getParameterName(), "Es un parametro obligatorio");
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_FALTANTE",
                "Falta el parametro obligatorio '" + ex.getParameterName() + "'", request, fieldErrors);
    }

    /**
     * Maneja un parametro (query param o path variable) cuyo valor no puede
     * convertirse
     * al tipo esperado, por ejemplo un id no numerico o un valor de enum invalido.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTipoInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String tipoEsperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "otro tipo";
        Map<String, String> fieldErrors = Map.of(ex.getName(), "Debe tener formato " + tipoEsperado);
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "El parametro '" + ex.getName() + "' tiene un formato invalido", request, fieldErrors);
    }

    /**
     * Maneja excepciones cuando se intenta ejecutar una plantilla de gasto
     * recurrente con el ciclo de revision vencido.
     *
     * @param ex Excepcion capturada.
     * @return Respuesta con codigo HTTP 409 Conflict.
     */
    @ExceptionHandler(CicloVencidoException.class)
    public ResponseEntity<String> handleCicloVencido(CicloVencidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // ==================== Autenticacion (401) ====================

    /**
     * Maneja excepciones cuando las credenciales de autenticacion son invalidas.
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponseDTO> handleCredencialesInvalidas(CredencialesInvalidasException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", ex.getMessage(), request);
    }

    // ==================== Acceso denegado (403) ====================

    /**
     * Maneja excepciones cuando un usuario autenticado no tiene permisos
     * suficientes.
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccesoDenegado(AccesoDenegadoException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", ex.getMessage(), request);
    }

    /**
     * Maneja el rechazo de acceso generado por Spring Security (por ejemplo,
     * via @PreAuthorize)
     * cuando ocurre dentro del despacho de un controlador, con el mismo contrato de
     * error.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", ex.getMessage(), request);
    }

    // ==================== Conflicto (409) ====================

    /**
     * Maneja cualquier excepcion de la categoria de conflicto (email duplicado,
     * membresia
     * duplicada, o futuras excepciones que extiendan ConflictoException).
     */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflicto(ConflictoException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, "CONFLICTO", ex.getMessage(), request);
    }

    /**
     * Maneja violaciones de integridad de datos a nivel de base. Solo se traduce a
     * 409 cuando
     * la causa raiz indica claramente una violacion de unicidad o clave duplicada;
     * cualquier
     * otra violacion de integridad (por ejemplo una clave foranea) se reporta como
     * 400,
     * ya que no es indiscriminadamente un duplicado.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleIntegridadDatos(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        if (esViolacionDeUnicidad(ex)) {
            return construir(HttpStatus.CONFLICT, "RECURSO_DUPLICADO",
                    "El recurso ya existe y viola una restriccion de unicidad", request);
        }
        return construir(HttpStatus.BAD_REQUEST, "INTEGRIDAD_DATOS",
                "La operacion viola una restriccion de integridad de los datos", request);
    }

    // ==================== Errores de framework (no deben caer en 500)
    // ====================

    /**
     * Maneja una solicitud realizada con un metodo HTTP no soportado por el
     * endpoint.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO", ex.getMessage(), request);
    }

    /**
     * Maneja una solicitud con un tipo de contenido no soportado por el endpoint.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMediaTypeNoSoportado(HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "MEDIA_TYPE_NO_SOPORTADO", ex.getMessage(), request);
    }

    // ==================== Error inesperado (500) ====================

    /**
     * Maneja cualquier excepcion generica no prevista, registrando el detalle del
     * lado del
     * servidor sin exponer informacion interna en la respuesta.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado no controlado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrio un error inesperado", request);
    }

    // ==================== Helpers ====================

    private boolean esViolacionDeUnicidad(DataIntegrityViolationException ex) {
        Throwable causaRaiz = ex.getMostSpecificCause();
        String mensaje = causaRaiz.getMessage();
        if (mensaje == null) {
            return false;
        }
        String mensajeMinuscula = mensaje.toLowerCase(Locale.ROOT);
        return mensajeMinuscula.contains("unique") || mensajeMinuscula.contains("duplicate")
                || mensajeMinuscula.contains("primary key");
    }

    private ResponseEntity<ErrorResponseDTO> construir(HttpStatus status, String code, String message,
            HttpServletRequest request) {
        return construir(status, code, message, request, null);
    }

    private ResponseEntity<ErrorResponseDTO> construir(HttpStatus status, String code, String message,
            HttpServletRequest request, Map<String, String> fieldErrors) {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
