package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.Gasto;
import com.example.fairshareapp.service.GastoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gestionar la API de gastos de la aplicacion FairShare.
 * Expone los endpoints HTTP para realizar operaciones CRUD sobre los gastos.
 */
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService gastoService;

    /**
     * Constructor para la inyeccion de dependencias del servicio de gastos.
     * 
     * @param gastoService Instancia del servicio de negocio de gastos.
     */
    @Autowired
    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    /**
     * Endpoint HTTP GET para listar todos los gastos registrados.
     * 
     * @return Lista de gastos con codigo HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Gasto>> obtenerTodosLosGastos() {
        List<Gasto> gastos = gastoService.obtenerTodos();
        return ResponseEntity.ok(gastos);
    }

    /**
     * Endpoint HTTP GET para buscar un gasto por su identificador unico.
     * 
     * @param id Identificador unico del gasto a consultar.
     * @return Gasto encontrado con HTTP 200 OK o HTTP 404 Not Found si no existe
     *         (lanzado como ResourceNotFoundException y traducido por el manejador global).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Gasto> obtenerGastoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(gastoService.obtenerPorIdOLanzar(id));
    }

    /**
     * Endpoint HTTP POST para registrar un nuevo gasto en el sistema.
     * 
     * @param gasto Objeto Gasto recibido en el cuerpo de la peticion HTTP.
     * @return Gasto guardado con codigo HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<Gasto> crearGasto(@RequestBody Gasto gasto) {
        Gasto gastoCreado = gastoService.guardar(gasto);
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoCreado);
    }

    /**
     * Endpoint HTTP PUT para actualizar un gasto existente.
     * 
     * @param id Identificador unico del gasto a actualizar.
     * @param gastoActualizado Datos actualizados del gasto.
     * @return Gasto modificado con HTTP 200 OK o HTTP 404 Not Found si no existe
     *         (lanzado como ResourceNotFoundException y traducido por el manejador global).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Gasto> actualizarGasto(@PathVariable Long id, @RequestBody Gasto gastoActualizado) {
        Gasto gastoExistente = gastoService.obtenerPorIdOLanzar(id);
        gastoExistente.setDescripcion(gastoActualizado.getDescripcion());
        gastoExistente.setMonto(gastoActualizado.getMonto());
        gastoExistente.setPagador(gastoActualizado.getPagador());
        gastoExistente.setFecha(gastoActualizado.getFecha());
        return ResponseEntity.ok(gastoService.guardar(gastoExistente));
    }

    /**
     * Endpoint HTTP DELETE para eliminar un gasto por su ID.
     * 
     * @param id Identificador unico del gasto a eliminar.
     * @return Codigo HTTP 204 No Content si se elimino correctamente, o 404 Not Found si no existia
     *         (lanzado por el servicio como GastoNoEncontradoException).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable Long id) {
        gastoService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }
}
