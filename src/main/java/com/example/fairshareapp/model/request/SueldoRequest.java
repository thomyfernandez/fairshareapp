package com.example.fairshareapp.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;

@Data
public class SueldoRequest {

    /**
     * Identificador del usuario propietario del sueldo. Es opcional en el body:
     * si no se envia, se utiliza la identidad autenticada del solicitante.
     */
    private Long usuarioId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "El tipo de sueldo es obligatorio")
    private TipoSueldo tipo;

    @NotNull(message = "La frecuencia del sueldo es obligatoria")
    private FrecuenciaSueldo frecuencia;

    /**
     * Mes del periodo (1 a 12). Opcional: si se omite se utiliza el mes actual.
     */
    private Integer mes;

    /**
     * Anio del periodo. Opcional: si se omite se utiliza el anio actual.
     */
    private Integer anio;
}
