package com.example.fairshareapp.model.request;

import lombok.Data;
import java.math.BigDecimal;

import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;

@Data
public class SueldoRequest {
    private Long usuarioId;
    private BigDecimal monto;
    private TipoSueldo tipo;
    private FrecuenciaSueldo frecuencia;
    private Integer mes;
    private Integer anio;
}
