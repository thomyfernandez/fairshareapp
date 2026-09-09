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
 * Entidad JPA que representa una transferencia minima entre dos usuarios,
 * resultado del calculo de liquidacion de un periodo cerrado.
 */
@Entity
@Table(name = "liquidacion_transferencias")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionTransferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    @JsonIgnore
    private Liquidacion liquidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deudor_id", nullable = false)
    private Usuario deudor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acreedor_id", nullable = false)
    private Usuario acreedor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;
}
