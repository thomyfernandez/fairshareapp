package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.EspacioCreateDTO;
import com.example.fairshareapp.model.dto.EspacioResponseDTO;
import com.example.fairshareapp.model.dto.EspacioUpdateDTO;
import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.service.EspacioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para la administracion y configuracion de espacios compartidos.
 * Expone operaciones de creacion, consulta, modificacion de reglas de distribucion y presupuesto base.
 */
@RestController
@RequestMapping("/api/v1/espacios")
public class EspacioController {

    private final EspacioService espacioService;

    /**
     * Constructor con inyeccion de dependencia del servicio de espacios.
     *
     * @param espacioService Servicio de espacios compartidos.
     */
    public EspacioController(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    /**
     * Registra un nuevo espacio compartido en el sistema.
     *
     * @param createDTO Datos del espacio a crear.
     * @return ResponseEntity con el EspacioResponseDTO creado y codigo HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<EspacioResponseDTO> crearEspacio(@Valid @RequestBody EspacioCreateDTO createDTO) {
        EspacioResponseDTO response = espacioService.crearEspacio(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene los detalles de un espacio mediante su identificador unico.
     *
     * @param id Identificador unico del espacio.
     * @return ResponseEntity con el EspacioResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EspacioResponseDTO> obtenerEspacioPorId(@PathVariable Long id) {
        EspacioResponseDTO response = espacioService.obtenerEspacioPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza la informacion de un espacio existente, incluyendo regla de reparto y presupuesto base.
     *
     * @param id Identificador unico del espacio a actualizar.
     * @param updateDTO Datos actualizados del espacio.
     * @return ResponseEntity con el EspacioResponseDTO actualizado y codigo HTTP 200 OK.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EspacioResponseDTO> actualizarEspacio(@PathVariable Long id,
                                                                @Valid @RequestBody EspacioUpdateDTO updateDTO) {
        EspacioResponseDTO response = espacioService.actualizarEspacio(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna el listado completo de los espacios disponibles.
     *
     * @return ResponseEntity con la lista de EspacioResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<EspacioResponseDTO>> listarEspacios() {
        List<EspacioResponseDTO> response = espacioService.listarEspacios();
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza especificamente la regla de reparto o distribucion de gastos (50/50 vs. Proporcional).
     *
     * @param id Identificador unico del espacio.
     * @param reglaReparto Nueva regla de distribucion seleccionada.
     * @return ResponseEntity con el EspacioResponseDTO modificado.
     */
    @PatchMapping("/{id}/regla-distribucion")
    public ResponseEntity<EspacioResponseDTO> editarReglaDistribucion(@PathVariable Long id,
                                                                     @RequestParam ReglaReparto reglaReparto) {
        EspacioResponseDTO response = espacioService.editarReglaDistribucion(id, reglaReparto);
        return ResponseEntity.ok(response);
    }

    /**
     * Fija o ajusta el presupuesto base asignado a los gastos del espacio.
     *
     * @param id Identificador unico del espacio.
     * @param presupuestoBase Monto del presupuesto base a establecer.
     * @return ResponseEntity con el EspacioResponseDTO modificado.
     */
    @PatchMapping("/{id}/presupuesto-base")
    public ResponseEntity<EspacioResponseDTO> fijarPresupuestoBase(@PathVariable Long id,
                                                                  @RequestParam BigDecimal presupuestoBase) {
        EspacioResponseDTO response = espacioService.fijarPresupuestoBase(id, presupuestoBase);
        return ResponseEntity.ok(response);
    }
}
