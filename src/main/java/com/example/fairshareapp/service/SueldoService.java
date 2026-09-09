package com.example.fairshareapp.service;

import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import java.util.List;

public interface SueldoService {

    SueldoResponse crearSueldo(Long usuarioId, SueldoRequest sueldoRequest);

    SueldoResponse crearSueldoCopia(Sueldo copia);

    SueldoResponse obtenerSueldo(Long id);

    List<SueldoResponse> obtenerSueldosPorUsuario(Long usuarioId);

    void actualizarSueldo(Long id, SueldoRequest sueldoRequest);

    void eliminarSueldo(Long id);

    List<SueldoResponse> getAllSueldos();

    Sueldo updateSueldo(Long id, SueldoResponse sueldo);

    void deleteSueldo(Long id);
}
