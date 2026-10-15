package com.example.fairshareapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;

@Entity
@Table(name = "sueldos", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sueldo_usuario_anio_mes", columnNames = {"usuario_id", "anio", "mes"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sueldo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private TipoSueldo tipo;

    @Column(nullable = false)
    private FrecuenciaSueldo frecuencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;
}
