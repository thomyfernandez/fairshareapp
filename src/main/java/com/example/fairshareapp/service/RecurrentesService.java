package com.example.fairshareapp.service;

import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import com.example.fairshareapp.model.dto.PlantillaGastoDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.repository.PlantillaGastoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service // Esto le avisa a Spring Boot que esta clase es un "Cerebro"
public class RecurrentesService {

    @Autowired
    private PlantillaGastoRepository plantillaRepository; // Conectamos nuestro control remoto

    // 1. Guardar un gasto como favorito (Plantilla)
    public PlantillaGastoRecurrente guardarFavorito(PlantillaGastoDTO dto) {
        PlantillaGastoRecurrente plantilla = new PlantillaGastoRecurrente();
        plantilla.setNombre(dto.getNombre());
        plantilla.setMontoBase(dto.getMontoBase());
        plantilla.setMontoVariable(dto.getMontoVariable());
        plantilla.setFrecuenciaAjusteMeses(dto.getFrecuenciaAjusteMeses());
        plantilla.setFechaProximaRevision(dto.getFechaProximaRevision());
        plantilla.setEspacioId(dto.getEspacioId());
        
        return plantillaRepository.save(plantilla); // Lo guarda en la base de datos
    }

    // 2. Eliminar un favorito
    public void eliminarFavorito(Long id) {
        plantillaRepository.deleteById(id);
    }

    // 3. Detectar vencimientos (Si la fecha de revisión ya pasó)
    public List<ServicioVencimientoDTO> detectarVencimientos(Long espacioId) {
        List<PlantillaGastoRecurrente> plantillas = plantillaRepository.findByEspacioId(espacioId);
        List<ServicioVencimientoDTO> vencidos = new ArrayList<>();

        for (PlantillaGastoRecurrente p : plantillas) {
            // Comparamos la fecha de hoy con la fecha de revisión
            if (p.getFechaProximaRevision() != null && !p.getFechaProximaRevision().isAfter(LocalDate.now())) {
                ServicioVencimientoDTO dto = new ServicioVencimientoDTO();
                dto.setServicioId(p.getId());
                dto.setNombreServicio(p.getNombre());
                dto.setFechaVencimiento(p.getFechaProximaRevision());
                vencidos.add(dto); // Lo agregamos a la lista de vencidos
            }
        }
        return vencidos;
    }
    
    // 4. Disparar un gasto rápido en 1 clic
    public void ejecutarGastoDesdePlantilla(Long id) {
        // NOTA:metodo preparado para que desarrolle thomas 
        
    }
}