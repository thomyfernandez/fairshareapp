package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.ProcesarLiquidacionDTO;
import com.example.fairshareapp.service.LiquidacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la administracion de liquidaciones y cortes presupuestarios de espacios compartidos.
 * Expone operaciones para procesar el checkout de gastos y consultar historiales de cierres.
 */
@RestController
@RequestMapping("/api/v1/espacios/{id}/liquidaciones")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;

    /**
     * Constructor con inyeccion del servicio de liquidaciones.
     *
     * @param liquidacionService Servicio de negocio de liquidaciones.
     */
    public LiquidacionController(LiquidacionService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    /**
     * Procesa el corte y cierre de liquidacion de los gastos pendientes de un espacio.
     * Valida el presupuesto disponible, descuenta el total y transiciona los gastos a LIQUIDADO.
     *
     * @param espacioId Identificador unico del espacio.
     * @param dto Parametros opcionales con seleccion de gastos, periodo y notas de cierre.
     * @return ResponseEntity con el detalle y resumen cuantitativo de la liquidacion y codigo HTTP 201 Created.
     */
    @PostMapping("/cierre")
    public ResponseEntity<LiquidacionResponseDTO> procesarCierre(
            @PathVariable("id") Long espacioId,
            @RequestBody(required = false) ProcesarLiquidacionDTO dto) {
        ProcesarLiquidacionDTO body = dto != null ? dto : new ProcesarLiquidacionDTO();
        LiquidacionResponseDTO response = liquidacionService.procesarCierreLiquidacion(espacioId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta el historial de liquidaciones cerradas en un espacio con filtros opcionales.
     *
     * @param espacioId Identificador unico del espacio.
     * @param desde Fecha de inicio opcional (formato YYYY-MM-DD).
     * @param hasta Fecha de fin opcional (formato YYYY-MM-DD).
     * @param anio Anio del periodo opcional.
     * @param mes Mes del periodo opcional (1 a 12).
     * @return ResponseEntity con la lista de liquidaciones historicas y codigo HTTP 200 OK.
     */
    @GetMapping("/historial")
    public ResponseEntity<List<LiquidacionResponseDTO>> obtenerHistorial(
            @PathVariable("id") Long espacioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {
        List<LiquidacionResponseDTO> historial = liquidacionService.obtenerHistorial(espacioId, desde, hasta, anio, mes);
        return ResponseEntity.ok(historial);
    }
}
