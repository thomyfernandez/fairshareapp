package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.service.SueldoService;
import com.example.fairshareapp.model.mapper.SueldoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sueldos")
public class SueldoController {

    private final SueldoService sueldoService;

    private final SueldoMapper sueldoMapper;

    /**
     * Constructor con inyeccion de dependencias de servicio y mapper de sueldos.
     *
     * @param sueldoService Servicio para la gestion de sueldos.
     * @param sueldoMapper Mapper para la transformacion de entidades y DTOs de sueldo.
     */
    public SueldoController(SueldoService sueldoService, SueldoMapper sueldoMapper) {
        this.sueldoService = sueldoService;
        this.sueldoMapper = sueldoMapper;
    }

    /**
     * Retorna el listado de sueldos registrados, filtrando opcionalmente por usuario.
     *
     * @param usuarioId Identificador opcional del usuario para filtrar sueldos.
     * @return Lista de SueldoResponse.
     */
    @GetMapping
    public List<SueldoResponse> getAllSueldos(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId != null) {
            return sueldoService.obtenerSueldosPorUsuario(usuarioId);
        }
        return sueldoService.getAllSueldos();
    }

    /**
     * Registra o actualiza el sueldo para un usuario y período mensual específico.
     *
     * @param request Datos del sueldo incluyendo monto, frecuencia, mes, anio y usuarioId opcional.
     * @return ResponseEntity con el SueldoResponse creado o actualizado.
     */
    @PostMapping
    public ResponseEntity<SueldoResponse> crearSueldo(@RequestBody SueldoRequest request) {
        Long usuarioId = request.getUsuarioId() != null ? request.getUsuarioId() : 1L;
        SueldoResponse response = sueldoService.crearSueldo(usuarioId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los datos de un sueldo existente identificado por su ID.
     *
     * @param id Identificador unico del sueldo a modificar.
     * @param sueldoRequest Nuevos datos del sueldo.
     * @return SueldoResponse actualizado.
     */
    @PutMapping("/{id}")
    public SueldoResponse updateSueldo(@PathVariable Long id, @RequestBody SueldoRequest sueldoRequest) {
        SueldoResponse sueldo = sueldoMapper.toSueldoResponse(sueldoRequest);
        return sueldoService.updateSueldo(id, sueldo).toSueldoResponse();
    }

    /**
     * Elimina un registro de sueldo segun su identificador unico.
     *
     * @param id Identificador unico del sueldo a borrar.
     */
    @DeleteMapping("/{id}")
    public void deleteSueldo(@PathVariable Long id) {
        sueldoService.deleteSueldo(id);
    }
}