package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta que expone la informacion consolidada de un espacio compartido.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String codigo;
    private TipoEspacio tipo;
    private ReglaReparto reglaReparto;
    private BigDecimal presupuestoBase;
}
