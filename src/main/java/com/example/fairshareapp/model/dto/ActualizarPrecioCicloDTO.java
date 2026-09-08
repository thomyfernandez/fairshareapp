package com.example.fairshareapp.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ActualizarPrecioCicloDTO {
    private BigDecimal nuevoMontoBase;
    private BigDecimal nuevoMontoVariable;
}