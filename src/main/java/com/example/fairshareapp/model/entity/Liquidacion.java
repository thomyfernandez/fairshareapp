package com.example.fairshareapp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una liquidacion formal o corte presupuestario de gastos en un espacio.
 * Agrupa los gastos cerrados y descuenta el total consumido del presupuesto base del espacio.
 */
@Entity
@Table(name = "liquidaciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fecha_liquidacion", nullable = false)
    private LocalDate fechaLiquidacion;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "liquidacion")
    @Builder.Default
    private List<Gasto> gastos = new ArrayList<>();
}
