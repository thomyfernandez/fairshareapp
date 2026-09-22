package com.example.fairshareapp.service;

import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.model.response.SueldoUpsertResult;
import java.util.List;

public interface SueldoService {

    /**
     * Crea el sueldo de un usuario para un periodo, o actualiza el registro existente
     * de ese mismo usuario/anio/mes (upsert).
     *
     * @param usuarioId Identificador del usuario propietario del sueldo.
     * @param sueldoRequest Datos del sueldo a registrar.
     * @return Resultado indicando el DTO resultante y si el registro fue creado o actualizado.
     */
    SueldoUpsertResult crearOActualizarSueldo(Long usuarioId, SueldoRequest sueldoRequest);

    SueldoResponse obtenerSueldo(Long id);

    List<SueldoResponse> obtenerSueldosPorUsuario(Long usuarioId);

    List<SueldoResponse> obtenerTodos();

    SueldoResponse actualizarSueldo(Long id, SueldoRequest sueldoRequest);

    void eliminarSueldo(Long id);
}
