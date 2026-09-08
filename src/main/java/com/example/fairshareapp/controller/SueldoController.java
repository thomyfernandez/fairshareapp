package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.service.SueldoService;
import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.mapper.SueldoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sueldos")
public class SueldoController {

    private final SueldoService sueldoService;

    private final SueldoMapper sueldoMapper;

    SueldoController(SueldoService sueldoService, SueldoMapper sueldoMapper) {
        this.sueldoService = sueldoService;
        this.sueldoMapper = sueldoMapper;
    }

    @GetMapping
    public List<SueldoResponse> getAllSueldos() {

        return sueldoService.getAllSueldos();

    }

    @PostMapping
    public ResponseEntity<SueldoResponse> crearSueldo(@RequestBody SueldoRequest request) {
        // El servicio ya devuelve el objeto mapeado correctamente
        SueldoResponse response = sueldoService.crearSueldo(Long.valueOf(1), request); // estatico con ID = 1, a cambiar
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public SueldoResponse updateSueldo(@PathVariable Long id, @RequestBody SueldoRequest sueldoRequest) {
        SueldoResponse sueldo = sueldoMapper.toSueldoResponse(sueldoRequest);
        SueldoResponse updatedSueldo = sueldoService.updateSueldo(id, sueldo).toSueldoResponse();
        return updatedSueldo;
    }

    @DeleteMapping("/{id}")
    public void deleteSueldo(@PathVariable Long id) {
        sueldoService.deleteSueldo(id);
    }
}