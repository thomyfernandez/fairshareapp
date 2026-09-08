package com.example.fairshareapp.model.entity;

import com.example.fairshareapp.model.enums.RolMiembro;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA asociativa que representa la pertenencia de un usuario a un espacio compartido.
 * Modela la tabla 'miembros_espacio', almacenando el rol del miembro dentro del espacio
 * y el sueldo declarado utilizado para el reparto proporcional de gastos.
 */
@Entity
@Table(name = "miembros_espacio",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "espacio_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiembroEspacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "El espacio es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    @JsonIgnore
    private Espacio espacio;

    @NotNull(message = "El rol del miembro es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolMiembro rol;

    @DecimalMin(value = "0.0", inclusive = true, message = "El sueldo declarado no puede ser negativo")
    @Column(name = "sueldo_declarado", precision = 12, scale = 2)
    private BigDecimal sueldoDeclarado;
}
