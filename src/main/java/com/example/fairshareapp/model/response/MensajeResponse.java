package com.example.fairshareapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de transferencia de datos generico para respuestas con un unico mensaje informativo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponse {

    private String message;
}
