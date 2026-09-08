package com.example.fairshareapp.model.response;

import lombok.Data;
import java.math.BigDecimal;

import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;

@Data
public class SueldoResponse {
    private Long id;
    private BigDecimal monto;
    private TipoSueldo tipo;
    private FrecuenciaSueldo frecuencia;
    private String usuarioNombre;

    // toSueldo method

}