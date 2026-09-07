package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.service.GastoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la administracion de gastos y particiones en los espacios.
 * Proporciona endpoints para el registro individual o por lote, consulta por periodos y eliminacion.
 */
@RestController
@RequestMapping("/api/v1")
public class GastoController {

    private final GastoService gastoService;

    /**
     * Constructor con inyeccion de dependencias del servicio de gastos.
     *
     * @param gastoService Instancia del servicio de logica de negocio de gastos.
     */
    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    /**
     * Registra un nuevo gasto dentro de un espacio especifico.
     *
     * @param id Identificador unico del espacio.
     * @param dto Informacion para la creacion del gasto y sus participantes.
     * @return Detalle del gasto creado con codigo HTTP 201 Created.
     */
    @PostMapping("/espacios/{id}/gastos")
    public ResponseEntity<GastoDetalleDTO> registrarGasto(@PathVariable Long id,
                                                          @RequestBody CrearGastoDTO dto) {
        GastoDetalleDTO gastoCreado = gastoService.registrarGasto(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoCreado);
    }

    /**
     * Registra un conjunto o lote de gastos en un espacio de forma atomica.
     *
     * @param id Identificador unico del espacio.
     * @param dtos Lista de gastos a registrar en el periodo.
     * @return Lista de gastos registrados con codigo HTTP 201 Created.
     */
    @PostMapping("/espacios/{id}/gastos/lote")
    public ResponseEntity<List<GastoDetalleDTO>> registrarLoteGastos(@PathVariable Long id,
                                                                     @RequestBody List<CrearGastoDTO> dtos) {
        List<GastoDetalleDTO> gastosCreados = gastoService.registrarLoteGastos(id, dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(gastosCreados);
    }

    /**
     * Obtiene el listado de gastos de un espacio, permitiendo opcionalmente filtrar por un rango de fechas.
     *
     * @param id Identificador unico del espacio.
     * @param desde Fecha de inicio del periodo a filtrar (opcional).
     * @param hasta Fecha de fin del periodo a filtrar (opcional).
     * @return Lista de gastos del espacio o del lote filtrado con codigo HTTP 200 OK.
     */
    @GetMapping("/espacios/{id}/gastos")
    public ResponseEntity<List<GastoDetalleDTO>> obtenerGastosPorEspacio(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<GastoDetalleDTO> gastos = gastoService.obtenerGastosPorEspacioYPeriodo(id, desde, hasta);
        return ResponseEntity.ok(gastos);
    }

    /**
     * Consulta el detalle especifico de un gasto a partir de su identificador unico.
     *
     * @param id Identificador unico del gasto.
     * @return Detalle del gasto con codigo HTTP 200 OK.
     */
    @GetMapping("/gastos/{id}")
    public ResponseEntity<GastoDetalleDTO> obtenerGastoPorId(@PathVariable Long id) {
        GastoDetalleDTO gasto = gastoService.obtenerGastoPorId(id);
        return ResponseEntity.ok(gasto);
    }

    /**
     * Elimina un gasto del sistema a partir de su identificador unico.
     *
     * @param id Identificador unico del gasto a eliminar.
     * @return Respuesta vacia con codigo HTTP 204 No Content.
     */
    @DeleteMapping("/gastos/{id}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable Long id) {
        gastoService.eliminarGasto(id);
        return ResponseEntity.noContent().build();
    }
}
