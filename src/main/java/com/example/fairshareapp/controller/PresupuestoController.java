package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.PresupuestoResponseDTO;
import com.example.fairshareapp.model.dto.PresupuestoUpsertDTO;
import com.example.fairshareapp.service.PresupuestoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la administracion del presupuesto mensual de cada usuario en un espacio.
 * Expone la fijacion (creacion o actualizacion) y consulta de presupuestos por usuario y periodo.
 */
@RestController
@RequestMapping("/api/v1/espacios/{espacioId}/presupuestos")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    /**
     * Constructor con inyeccion de dependencia del servicio de presupuestos.
     *
     * @param presupuestoService Servicio de negocio de presupuestos.
     */
    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    /**
     * Fija (crea o actualiza) el presupuesto mensual de un usuario dentro de un espacio.
     *
     * @param espacioId Identificador unico del espacio.
     * @param usuarioId Identificador unico del usuario.
     * @param anio Anio del presupuesto a fijar.
     * @param mes Mes del presupuesto a fijar.
     * @param dto Datos del presupuesto a fijar.
     * @return ResponseEntity con el PresupuestoResponseDTO resultante y codigo HTTP 200 OK.
     */
    @PutMapping("/usuarios/{usuarioId}")
    public ResponseEntity<PresupuestoResponseDTO> fijarPresupuesto(@PathVariable Long espacioId,
                                                                    @PathVariable Long usuarioId,
                                                                    @RequestParam Integer anio,
                                                                    @RequestParam Integer mes,
                                                                    @Valid @RequestBody PresupuestoUpsertDTO dto) {
        PresupuestoResponseDTO response = presupuestoService.fijarPresupuesto(espacioId, usuarioId, anio, mes, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista los presupuestos cargados en un espacio, filtrando opcionalmente por anio y mes.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio a filtrar (opcional).
     * @param mes Mes a filtrar (opcional).
     * @return ResponseEntity con la lista de PresupuestoResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<PresupuestoResponseDTO>> listarPresupuestos(@PathVariable Long espacioId,
                                                                            @RequestParam(required = false) Integer anio,
                                                                            @RequestParam(required = false) Integer mes) {
        return ResponseEntity.ok(presupuestoService.listarPresupuestos(espacioId, anio, mes));
    }
}
