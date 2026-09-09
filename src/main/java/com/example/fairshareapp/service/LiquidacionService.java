package com.example.fairshareapp.service;

import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.ProcesarLiquidacionDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de negocio para la gestion y procesamiento de liquidaciones en espacios compartidos.
 */
public interface LiquidacionService {

    /**
     * Procesa el corte presupuestario y cierre de liquidacion para los gastos pendientes de un espacio.
     * Valida la disponibilidad de presupuesto base en el espacio, descuenta el importe consumido
     * y transiciona los gastos al estado LIQUIDADO.
     *
     * @param espacioId Identificador unico del espacio.
     * @param dto Datos del cierre incluyendo gastos seleccionados opcionales, periodo y notas.
     * @return LiquidacionResponseDTO con el detalle y resumen del cierre efectuado.
     */
    LiquidacionResponseDTO procesarCierreLiquidacion(Long espacioId, ProcesarLiquidacionDTO dto);

    /**
     * Obtiene el historial de liquidaciones cerradas en un espacio, permitiendo filtrar por fechas o mes y anio.
     *
     * @param espacioId Identificador unico del espacio.
     * @param desde Fecha de inicio de filtrado opcional.
     * @param hasta Fecha de fin de filtrado opcional.
     * @param anio Anio de filtrado opcional.
     * @param mes Mes de filtrado opcional.
     * @return Lista de liquidaciones correspondientes al criterio solicitado.
     */
    List<LiquidacionResponseDTO> obtenerHistorial(Long espacioId, LocalDate desde, LocalDate hasta, Integer anio, Integer mes);
}
