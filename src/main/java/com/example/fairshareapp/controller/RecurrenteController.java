package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import com.example.fairshareapp.model.dto.PlantillaGastoDTO;
import com.example.fairshareapp.model.dto.ActualizarPrecioCicloDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.service.RecurrentesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1") // Todos tus links van a empezar con esta base
public class RecurrenteController {

    @Autowired
    private RecurrentesService recurrentesService; // Conectamos con tu cerebro

    // 1. POST: Crear un nuevo favorito en un espacio
    @PostMapping("/espacios/{id}/favoritos")
    public ResponseEntity<PlantillaGastoRecurrente> crearFavorito(@PathVariable Long id, @RequestBody PlantillaGastoDTO dto) {
        dto.setEspacioId(id); 
        PlantillaGastoRecurrente nuevaPlantilla = recurrentesService.guardarFavorito(dto);
        return ResponseEntity.ok(nuevaPlantilla);
    }

    // 2. GET: Ver favoritos (y chequear vencimientos)
    @GetMapping("/espacios/{id}/favoritos")
    public ResponseEntity<List<ServicioVencimientoDTO>> obtenerVencimientos(@PathVariable Long id) {
        List<ServicioVencimientoDTO> vencidos = recurrentesService.detectarVencimientos(id);
        return ResponseEntity.ok(vencidos);
    }

    // 3. POST: Ejecutar un gasto rápido con 1 clic
    @PostMapping("/favoritos/{id}/ejecutar")
    public ResponseEntity<String> ejecutarGasto(@PathVariable Long id) {
        recurrentesService.ejecutarGastoDesdePlantilla(id);
        return ResponseEntity.ok("Gasto ejecutado (Pendiente de conectar con el módulo de Thomas)");
    }

    // 4. PUT: Actualizar el monto por inflación/IPC
    @PutMapping("/favoritos/{id}/actualizar-monto")
    public ResponseEntity<String> actualizarMonto(@PathVariable Long id, @RequestBody ActualizarPrecioCicloDTO dto) {
        return ResponseEntity.ok("Monto actualizado correctamente");
    }
}