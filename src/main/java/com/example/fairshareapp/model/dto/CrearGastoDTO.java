package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.entity.ReglaDivision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Objeto de transferencia de datos para la solicitud de creacion de un gasto.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearGastoDTO {

    private String descripcion;
    private Double monto;
    private LocalDate fecha;
    private Long pagadorId;
    private Long categoriaId;
    private ReglaDivision regla;
    private List<GastoParticipanteDTO> participantes;
}
