package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.ActualizarPrecioCicloDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.service.RecurrentesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la administracion de plantillas de gastos recurrentes y favoritos.
 * Permite registrar y consultar favoritos, detectar vencimientos de ciclos tarifarios,
 * actualizar importes por inflacion y disparar la creacion rapida de gastos en 1 clic.
 */
@RestController
@RequestMapping("/api/v1")
public class RecurrenteController {

    private final RecurrentesService recurrentesService;

    /**
     * Constructor con inyeccion de dependencias del servicio de gastos recurrentes.
     *
     * @param recurrentesService Instancia del servicio con logica de negocio para favoritos y recurrentes.
     */
    public RecurrenteController(RecurrentesService recurrentesService) {
        this.recurrentesService = recurrentesService;
    }

    /**
     * Registra una nueva plantilla de gasto como favorita dentro de un espacio compartido.
     *
     * @param id Identificador unico del espacio.
     * @param dto Datos de la plantilla a registrar.
     * @return Plantilla creada con codigo HTTP 201 Created.
     */
    @PostMapping("/espacios/{id}/favoritos")
    public ResponseEntity<PlantillaGastoDTO> crearFavorito(@PathVariable Long id,
                                                           @Valid @RequestBody PlantillaGastoDTO dto) {
        dto.setEspacioId(id);
        PlantillaGastoDTO nuevaPlantilla = recurrentesService.guardarFavorito(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPlantilla);
    }

    /**
     * Obtiene el listado de plantillas favoritas asociadas a un espacio determinado.
     * Permite filtrar opcionalmente solo aquellas que se encuentran vencidas.
     *
     * @param id Identificador unico del espacio.
     * @param soloVencidos Indicador para retornar unicamente las plantillas con ciclo tarifario vencido.
     * @return Lista de plantillas favoritas segun el criterio solicitado.
     */
    @GetMapping("/espacios/{id}/favoritos")
    public ResponseEntity<List<PlantillaGastoDTO>> obtenerFavoritos(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean soloVencidos) {
        List<PlantillaGastoDTO> favoritos = recurrentesService.obtenerFavoritosPorEspacio(id);
        if (soloVencidos) {
            favoritos = favoritos.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getVencido()))
                    .toList();
        }
        return ResponseEntity.ok(favoritos);
    }

    /**
     * Consulta especificamente los vencimientos de ciclos tarifarios en un espacio compartido.
     *
     * @param id Identificador unico del espacio.
     * @return Lista de servicios y plantillas vencidas que exigen actualizacion de tarifa.
     */
    @GetMapping("/espacios/{id}/favoritos/vencimientos")
    public ResponseEntity<List<ServicioVencimientoDTO>> obtenerVencimientos(@PathVariable Long id) {
        List<ServicioVencimientoDTO> vencidos = recurrentesService.detectarVencimientos(id);
        return ResponseEntity.ok(vencidos);
    }

    /**
     * Consulta los detalles de una plantilla favorita a partir de su identificador unico.
     *
     * @param id Identificador unico de la plantilla.
     * @return Detalle de la plantilla encontrada con codigo HTTP 200 OK.
     */
    @GetMapping("/favoritos/{id}")
    public ResponseEntity<PlantillaGastoDTO> obtenerFavoritoPorId(@PathVariable Long id) {
        PlantillaGastoDTO dto = recurrentesService.obtenerFavoritoPorId(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Dispara la ejecucion rapida de un gasto en 1 clic a partir de la plantilla favorita configurada.
     * Exige previamente la actualizacion de tarifa si el ciclo de revision se encuentra vencido.
     *
     * @param id Identificador unico de la plantilla a ejecutar.
     * @return Detalle del gasto registrado con codigo HTTP 201 Created.
     */
    @PostMapping("/favoritos/{id}/ejecutar")
    public ResponseEntity<GastoDetalleDTO> ejecutarGasto(@PathVariable Long id) {
        GastoDetalleDTO gastoCreado = recurrentesService.ejecutarGastoDesdePlantilla(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoCreado);
    }

    /**
     * Actualiza el monto base y variable de un ciclo tarifario (por ejemplo por ajuste de IPC o alquiler).
     *
     * @param id Identificador unico de la plantilla a actualizar.
     * @param dto Informacion con los nuevos montos y fecha de ciclo.
     * @return Plantilla actualizada con codigo HTTP 200 OK.
     */
    @PutMapping("/favoritos/{id}/actualizar-monto")
    public ResponseEntity<PlantillaGastoDTO> actualizarMonto(@PathVariable Long id,
                                                             @Valid @RequestBody ActualizarPrecioCicloDTO dto) {
        PlantillaGastoDTO actualizada = recurrentesService.actualizarMonto(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Elimina una plantilla favorita del sistema.
     *
     * @param id Identificador unico de la plantilla a eliminar.
     * @return Respuesta vacia con codigo HTTP 204 No Content.
     */
    @DeleteMapping("/favoritos/{id}")
    public ResponseEntity<Void> eliminarFavorito(@PathVariable Long id) {
        recurrentesService.eliminarFavorito(id);
        return ResponseEntity.noContent().build();
    }
}