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
 * Entidad JPA que representa la participacion de un usuario especifico en un gasto.
 * Almacena la cuota parte o importe que le corresponde abonar segun la regla de division activa.
 */
@Entity
@Table(name = "gasto_participantes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoParticipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_id", nullable = false)
    @JsonIgnore
    private Gasto gasto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private BigDecimal importe;

    @Column
    private BigDecimal porcentaje;
}
