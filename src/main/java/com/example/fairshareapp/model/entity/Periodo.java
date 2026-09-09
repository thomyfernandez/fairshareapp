package com.example.fairshareapp.model.entity;

import com.example.fairshareapp.model.enums.EstadoPeriodo;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Entidad JPA que representa un periodo mensual de un espacio compartido.
 * Modela la tabla 'periodos' en la base de datos: mientras esta ABIERTO admite el registro
 * de nuevos gastos fechados dentro de su rango; al cerrarse dispara la generacion de su Liquidacion.
 */
@Entity
@Table(name = "periodos", uniqueConstraints = @UniqueConstraint(columnNames = {"espacio_id", "anio", "mes"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Periodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @NotNull(message = "El anio del periodo es obligatorio")
    @Min(value = 2000, message = "El anio del periodo debe ser valido")
    @Column(nullable = false)
    private Integer anio;

    @NotNull(message = "El mes del periodo es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    @Column(nullable = false)
    private Integer mes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPeriodo estado;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    /**
     * Calcula la fecha del primer dia del mes que representa este periodo.
     *
     * @return Fecha de inicio del periodo (inclusive).
     */
    public LocalDate getFechaInicio() {
        return YearMonth.of(anio, mes).atDay(1);
    }

    /**
     * Calcula la fecha del ultimo dia del mes que representa este periodo.
     *
     * @return Fecha de fin del periodo (inclusive).
     */
    public LocalDate getFechaFin() {
        return YearMonth.of(anio, mes).atEndOfMonth();
    }
}
