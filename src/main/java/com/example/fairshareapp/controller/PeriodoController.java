package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.PeriodoCreateDTO;
import com.example.fairshareapp.model.dto.PeriodoResponseDTO;
import com.example.fairshareapp.service.PeriodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la administracion de periodos mensuales de un espacio compartido.
 * Expone la apertura, consulta y cierre de periodos; el cierre dispara la generacion de su liquidacion.
 */
@RestController
@RequestMapping("/api/v1/espacios/{espacioId}/periodos")
public class PeriodoController {

    private final PeriodoService periodoService;

    /**
     * Constructor con inyeccion de dependencia del servicio de periodos.
     *
     * @param periodoService Servicio de negocio de periodos.
     */
    public PeriodoController(PeriodoService periodoService) {
        this.periodoService = periodoService;
    }

    /**
     * Abre un nuevo periodo mensual para un espacio.
     *
     * @param espacioId Identificador unico del espacio.
     * @param dto Datos del periodo a abrir.
     * @return ResponseEntity con el PeriodoResponseDTO creado y codigo HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<PeriodoResponseDTO> abrirPeriodo(@PathVariable Long espacioId,
                                                            @Valid @RequestBody PeriodoCreateDTO dto) {
        PeriodoResponseDTO response = periodoService.abrirPeriodo(espacioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista los periodos de un espacio, del mas reciente al mas antiguo.
     *
     * @param espacioId Identificador unico del espacio.
     * @return ResponseEntity con la lista de PeriodoResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<PeriodoResponseDTO>> listarPeriodos(@PathVariable Long espacioId) {
        return ResponseEntity.ok(periodoService.listarPeriodos(espacioId));
    }

    /**
     * Obtiene el detalle de un periodo especifico de un espacio.
     *
     * @param espacioId Identificador unico del espacio (no utilizado para la busqueda, presente por consistencia de ruta).
     * @param periodoId Identificador unico del periodo.
     * @return ResponseEntity con el PeriodoResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping("/{periodoId}")
    public ResponseEntity<PeriodoResponseDTO> obtenerPeriodo(@PathVariable Long espacioId,
                                                              @PathVariable Long periodoId) {
        return ResponseEntity.ok(periodoService.obtenerPeriodoPorId(periodoId));
    }

    /**
     * Cierra un periodo abierto, bloqueando el alta de nuevos gastos en su rango de fechas
     * y generando la liquidacion final del periodo.
     *
     * @param espacioId Identificador unico del espacio (no utilizado para la operacion, presente por consistencia de ruta).
     * @param periodoId Identificador unico del periodo a cerrar.
     * @return ResponseEntity con el LiquidacionResponseDTO generado y codigo HTTP 200 OK.
     */
    @PostMapping("/{periodoId}/cierre")
    public ResponseEntity<LiquidacionResponseDTO> cerrarPeriodo(@PathVariable Long espacioId,
                                                                 @PathVariable Long periodoId) {
        return ResponseEntity.ok(periodoService.cerrarPeriodo(periodoId));
    }
}
