package com.example.fairshareapp.model.entity;

import com.example.fairshareapp.model.enums.ReglaDivision;
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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA que representa una plantilla de gasto recurrente o favorito en el sistema.
 * Permite configurar frecuencias de ajuste por IPC o alquiler, montos base y variables,
 * y fechas de revision de ciclo tarifario.
 */
@Entity
@Table(name = "plantillas_gastos_recurrentes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaGastoRecurrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "frecuencia_ajuste_meses")
    private Integer frecuenciaAjusteMeses;

    @Column(name = "monto_base", precision = 12, scale = 2)
    private BigDecimal montoBase;

    @Column(name = "monto_variable", precision = 12, scale = 2)
    private BigDecimal montoVariable;

    @Column(name = "fecha_proxima_revision")
    private LocalDate fechaProximaRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagador_id", nullable = false)
    private Usuario pagadorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "regla_division")
    private ReglaDivision reglaDivision;
}