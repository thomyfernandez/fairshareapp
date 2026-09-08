package com.example.fairshareapp.model.mapper;

import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SueldoMapper {

    SueldoMapper INSTANCE = Mappers.getMapper(SueldoMapper.class);

    default SueldoResponse toResponse(Sueldo sueldo) {
        SueldoResponse sueldoResponse = new SueldoResponse();
        sueldoResponse.setId(sueldo.getId());
        sueldoResponse.setMonto(sueldo.getMonto());
        sueldoResponse.setTipo(sueldo.getTipo());
        sueldoResponse.setFrecuencia(sueldo.getFrecuencia());

        return sueldoResponse;
    }

    // toSueldoResponse from Request
    SueldoResponse toSueldoResponse(SueldoRequest sueldoRequest);

    default List<SueldoResponse> toResponseList(List<Sueldo> sueldos) {
        return sueldos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    Sueldo toSueldo(SueldoRequest sueldoRequest);

}