package com.example.fairshareapp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

/**
 * Objeto de transferencia de datos con el contrato de error unico de la API.
 * Lo produce GlobalExceptionHandler para toda excepcion capturada en la aplicacion,
 * de modo que todos los clientes reciban siempre la misma forma de respuesta.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;

    /**
     * Detalle de errores por campo, presente unicamente cuando el error se origina
     * en una validacion de datos de entrada (DTO invalido, parametro invalido, etc.).
     */
    private Map<String, String> fieldErrors;
}
