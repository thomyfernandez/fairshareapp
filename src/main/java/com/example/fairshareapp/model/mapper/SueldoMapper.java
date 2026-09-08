package com.example.fairshareapp.model.mapper;

import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        if (sueldo.getUsuario() != null) {
            sueldoResponse.setUsuarioNombre(sueldo.getUsuario().getNombre());
        }

        return sueldoResponse;
    }

    /**
     * Convierte una solicitud SueldoRequest a SueldoResponse.
     *
     * @param sueldoRequest DTO de solicitud.
     * @return DTO SueldoResponse.
     */
    public SueldoResponse toSueldoResponse(SueldoRequest sueldoRequest) {
        if (sueldoRequest == null) {
            return null;
        }
        SueldoResponse response = new SueldoResponse();
        response.setMonto(sueldoRequest.getMonto());
        response.setTipo(sueldoRequest.getTipo());
        response.setFrecuencia(sueldoRequest.getFrecuencia());
        return response;
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
                .collect(Collectors.toList());
    }

    /**
     * Convierte una solicitud SueldoRequest a una nueva entidad Sueldo.
     *
     * @param sueldoRequest DTO de solicitud.
     * @return Entidad Sueldo construida.
     */
    public Sueldo toSueldo(SueldoRequest sueldoRequest) {
        if (sueldoRequest == null) {
            return null;
        }
        return Sueldo.builder()
                .monto(sueldoRequest.getMonto())
                .tipo(sueldoRequest.getTipo())
                .frecuencia(sueldoRequest.getFrecuencia())
                .build();
    }
}