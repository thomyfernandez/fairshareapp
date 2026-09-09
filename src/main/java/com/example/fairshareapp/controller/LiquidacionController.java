package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.service.LiquidacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la consulta de liquidaciones historicas y la previsualizacion
 * del calculo de liquidacion de un periodo aun abierto.
 */
@RestController
@RequestMapping("/api/v1")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;

    /**
     * Constructor con inyeccion de dependencia del servicio de liquidaciones.
     *
     * @param liquidacionService Servicio de negocio de liquidaciones.
     */
    public LiquidacionController(LiquidacionService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    /**
     * Lista las liquidaciones historicas generadas para un espacio, de la mas reciente a la mas antigua.
     *
     * @param espacioId Identificador unico del espacio.
     * @return ResponseEntity con la lista de LiquidacionResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping("/espacios/{espacioId}/liquidaciones")
    public ResponseEntity<List<LiquidacionResponseDTO>> listarLiquidaciones(@PathVariable Long espacioId) {
        return ResponseEntity.ok(liquidacionService.listarPorEspacio(espacioId));
    }

    /**
     * Previsualiza el calculo de liquidacion de un anio y mes de un espacio, sin persistirlo.
     * Util para consultar el estado de un periodo aun abierto o inexistente antes de cerrarlo.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio a previsualizar.
     * @param mes Mes a previsualizar.
     * @return ResponseEntity con el LiquidacionResponseDTO calculado (id nulo) y codigo HTTP 200 OK.
     */
    @GetMapping("/espacios/{espacioId}/liquidaciones/preview")
    public ResponseEntity<LiquidacionResponseDTO> previsualizarLiquidacion(@PathVariable Long espacioId,
                                                                            @RequestParam Integer anio,
                                                                            @RequestParam Integer mes) {
        return ResponseEntity.ok(liquidacionService.previsualizar(espacioId, anio, mes));
    }

    /**
     * Obtiene el detalle completo de una liquidacion por su identificador.
     *
     * @param liquidacionId Identificador unico de la liquidacion.
     * @return ResponseEntity con el LiquidacionResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping("/liquidaciones/{liquidacionId}")
    public ResponseEntity<LiquidacionResponseDTO> obtenerLiquidacion(@PathVariable Long liquidacionId) {
        return ResponseEntity.ok(liquidacionService.obtenerPorId(liquidacionId));
    }
}
