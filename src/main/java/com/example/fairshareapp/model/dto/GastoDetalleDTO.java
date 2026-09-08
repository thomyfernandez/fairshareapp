package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.enums.ReglaDivision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Objeto de transferencia de datos con el detalle estructurado de un gasto registrado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoDetalleDTO {

    private Long id;
    private String descripcion;
    private BigDecimal monto;
    private LocalDate fecha;
    private Long espacioId;
    private String espacioNombre;
    private Long pagadorId;
    private String pagadorNombre;
    private String pagadorEmail;
    private Long categoriaId;
    private String categoriaNombre;
    private ReglaDivision regla;
    private List<GastoParticipanteDTO> participantes;
}
