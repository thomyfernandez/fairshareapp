package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.ProcesarLiquidacionDTO;
import com.example.fairshareapp.service.LiquidacionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/espacios/{id}/liquidaciones")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;

    public LiquidacionController(LiquidacionService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    @PostMapping("/cierre")
    public ResponseEntity procesarCierre(
            @PathVariable("id") Long espacioId,
            @Valid @RequestBody(required = false) ProcesarLiquidacionDTO dto) {
        ProcesarLiquidacionDTO body = dto != null ? dto : new ProcesarLiquidacionDTO();
        LiquidacionResponseDTO response = liquidacionService.procesarCierreLiquidacion(espacioId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/historial")
    public ResponseEntity obtenerHistorial(
            @PathVariable("id") Long espacioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {
        
        var historial = liquidacionService.obtenerHistorial(espacioId, desde, hasta, anio, mes);
        return ResponseEntity.ok(historial);
    }
}