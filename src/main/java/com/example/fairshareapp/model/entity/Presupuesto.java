package com.example.fairshareapp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa el presupuesto mensual de gasto de un usuario dentro de un espacio.
 * Modela la tabla 'presupuestos' en la base de datos: al cerrar el periodo correspondiente,
 * se compara contra el total efectivamente gastado por el usuario para detectar excedentes.
 */
@Entity
@Table(name = "presupuestos", uniqueConstraints = @UniqueConstraint(columnNames = {"espacio_id", "usuario_id", "anio", "mes"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "El anio del presupuesto es obligatorio")
    @Min(value = 2000, message = "El anio del presupuesto debe ser valido")
    @Column(nullable = false)
    private Integer anio;

    @NotNull(message = "El mes del presupuesto es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    @Column(nullable = false)
    private Integer mes;

    @NotNull(message = "El monto limite del presupuesto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto limite debe ser un valor positivo")
    @Column(name = "monto_limite", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoLimite;
}
