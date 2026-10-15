package com.example.fairshareapp.model.mapper;

import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.response.SueldoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente para mapear entre la entidad Sueldo y sus correspondientes DTOs de Request y Response.
 */
@Component
public class SueldoMapper {

    /**
     * Convierte una entidad Sueldo a SueldoResponse.
     *
     * @param sueldo Entidad Sueldo.
     * @return DTO SueldoResponse correspondiente.
     */
    public SueldoResponse toResponse(Sueldo sueldo) {
        if (sueldo == null) {
            return null;
        }
        SueldoResponse sueldoResponse = new SueldoResponse();
        sueldoResponse.setId(sueldo.getId());
        sueldoResponse.setMonto(sueldo.getMonto());
        sueldoResponse.setTipo(sueldo.getTipo());
        sueldoResponse.setFrecuencia(sueldo.getFrecuencia());
        sueldoResponse.setMes(sueldo.getMes());
        sueldoResponse.setAnio(sueldo.getAnio());
        if (sueldo.getUsuario() != null) {
            sueldoResponse.setUsuarioId(sueldo.getUsuario().getId());
            sueldoResponse.setUsuarioNombre(sueldo.getUsuario().getNombre());
        }

        return sueldoResponse;
    }

    /**
     * Convierte una lista de entidades Sueldo a lista de SueldoResponse.
     *
     * @param sueldos Lista de entidades Sueldo.
     * @return Lista de DTOs SueldoResponse.
     */
    public List<SueldoResponse> toResponseList(List<Sueldo> sueldos) {
        if (sueldos == null) {
            return List.of();
        }
        return sueldos.stream()
                .map(this::toResponse)
                .toList();
    }
}
