package com.example.fairshareapp.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa la liquidacion historica generada al cerrar un periodo de un espacio.
 * Modela la tabla 'liquidaciones' en la base de datos: contiene las transferencias minimas
 * necesarias para saldar las deudas del periodo y el resumen de gasto/presupuesto de cada usuario.
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false, unique = true)
    private Periodo periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "monto_total_gastos", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotalGastos;

    @OneToMany(mappedBy = "liquidacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LiquidacionTransferencia> transferencias = new ArrayList<>();

    @OneToMany(mappedBy = "liquidacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LiquidacionResumenUsuario> resumenPorUsuario = new ArrayList<>();

    /**
     * Agrega una transferencia a la liquidacion, manteniendo la coherencia bidireccional de la relacion.
     *
     * @param transferencia Instancia de LiquidacionTransferencia a asociar.
     */
    public void agregarTransferencia(LiquidacionTransferencia transferencia) {
        transferencias.add(transferencia);
        transferencia.setLiquidacion(this);
    }

    /**
     * Agrega el resumen de gasto y presupuesto de un usuario a la liquidacion,
     * manteniendo la coherencia bidireccional de la relacion.
     *
     * @param resumen Instancia de LiquidacionResumenUsuario a asociar.
     */
    public void agregarResumenUsuario(LiquidacionResumenUsuario resumen) {
        resumenPorUsuario.add(resumen);
        resumen.setLiquidacion(this);
    }
}
