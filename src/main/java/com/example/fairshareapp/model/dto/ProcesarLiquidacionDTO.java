package com.example.fairshareapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para solicitar el procesamiento y cierre de una liquidacion en un espacio.
 * Permite definir un conjunto puntual de IDs de gastos o liquidar la totalidad de pendientes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarLiquidacionDTO {

    /**
     * Lista opcional de identificadores de gastos especificos a incluir en la liquidacion.
     * Si no se provee o esta vacia, se agrupan todos los gastos en estado PENDIENTE del espacio.
     */
    private List<Long> gastoIds;

    /**
     * Mes del periodo de liquidacion (1 a 12). Si no se provee, se toma el mes actual.
     */
    private Integer mes;

    /**
     * Anio del periodo de liquidacion. Si no se provee, se toma el anio actual.
     */
    private Integer anio;

    /**
     * Descripcion o nota aclaratoria del cierre de liquidacion.
     */
    private String descripcion;
}
