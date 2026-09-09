package com.example.fairshareapp.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa, dentro de una liquidacion, el resumen de gasto y presupuesto
 * de un usuario particular para el periodo liquidado.
 */
@Entity
@Table(name = "liquidacion_resumen_usuario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionResumenUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    @JsonIgnore
    private Liquidacion liquidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "total_gastado", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGastado;

    /**
     * Limite de presupuesto que tenia cargado el usuario para el periodo. Nulo si no habia
     * fijado un presupuesto, en cuyo caso no corresponde evaluar excedente.
     */
    @Column(name = "presupuesto_limite", precision = 12, scale = 2)
    private BigDecimal presupuestoLimite;

    /**
     * Indica si el gasto del usuario supero su presupuesto. Nulo si no tenia presupuesto cargado.
     */
    @Column(name = "presupuesto_excedido")
    private Boolean presupuestoExcedido;

    @Column(name = "monto_excedente", precision = 12, scale = 2)
    private BigDecimal montoExcedente;
}
