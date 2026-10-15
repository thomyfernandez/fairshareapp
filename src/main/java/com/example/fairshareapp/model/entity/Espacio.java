package com.example.fairshareapp.model.entity;

import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa un espacio compartido donde se registran gastos y miembros.
 * Modela la tabla 'espacios' en la base de datos con reglas de distribucion y presupuesto base.
 */
@Entity
@Table(name = "espacios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Espacio {
    @jakarta.persistence.Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del espacio es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede superar los 255 caracteres")
    @Column(length = 255)
    private String descripcion;

    @Size(max = 30, message = "El codigo no puede superar los 30 caracteres")
    @Column(unique = true, length = 30)
    private String codigo;

    @NotNull(message = "El tipo de espacio es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEspacio tipo;

    @NotNull(message = "La regla de reparto es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "regla_reparto", nullable = false, length = 30)
    private ReglaReparto reglaReparto;

    @DecimalMin(value = "0.0", inclusive = true, message = "El presupuesto base no puede ser negativo")
    @Column(name = "presupuesto_base", precision = 12, scale = 2)
    private BigDecimal presupuestoBase;
}

