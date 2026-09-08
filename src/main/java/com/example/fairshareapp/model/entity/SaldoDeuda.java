package com.example.fairshareapp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * Entidad JPA que representa la deuda simplificada de un usuario (deudor) hacia otro (acreedor)
 * dentro de un espacio compartido, resultante de la consolidacion de los gastos registrados.
 * Modela la tabla 'saldo_deudas' en la base de datos.
 */
@Entity
@Table(name = "saldo_deudas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaldoDeuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deudor_id", nullable = false)
    private Usuario deudor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acreedor_id", nullable = false)
    private Usuario acreedor;

    /**
     * Monto total que corresponde a esta relacion de deuda segun el ultimo recalculo
     * de balance. Sirve como referencia para incorporar deuda nueva sin resucitar
     * la porcion que ya fue pagada.
     */
    @Column(name = "monto_original", nullable = false)
    private Double montoOriginal;

    /**
     * Monto actualmente pendiente de pago. Disminuye a medida que se registran pagos
     * mediante el endpoint de saldar deuda.
     */
    @Column(nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDeuda estado;

    /**
     * Identificador de la liquidacion asociada a esta deuda, si corresponde.
     * Se guarda como referencia debil (sin relacion JPA) porque la entidad Liquidacion
     * es responsabilidad de otro modulo del proyecto y no debe modificarse desde aqui.
     */
    @Column(name = "liquidacion_id")
    private Long liquidacionId;
}
