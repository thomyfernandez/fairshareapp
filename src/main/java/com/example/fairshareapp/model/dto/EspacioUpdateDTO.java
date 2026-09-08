package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para la actualizacion de datos de un espacio compartido, incluyendo
 * la modificacion de reglas de distribucion (50/50 vs. Proporcional) y fijacion de presupuesto base.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioUpdateDTO {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede superar los 255 caracteres")
    private String descripcion;

    @Size(max = 30, message = "El codigo no puede superar los 30 caracteres")
    private String codigo;

    private TipoEspacio tipo;

    private ReglaReparto reglaReparto;

    @DecimalMin(value = "0.0", inclusive = true, message = "El presupuesto base no puede ser negativo")
    private BigDecimal presupuestoBase;
}
