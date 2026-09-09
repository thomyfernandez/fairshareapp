package com.example.fairshareapp.model.entity;

import com.example.fairshareapp.model.enums.ReglaDivision;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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
 * Entidad JPA que representa un gasto dentro de la aplicacion.
 * Modela la tabla 'gastos' en la base de datos vinculada con Espacio, Categoria, Usuario (pagador)
 * y una coleccion de participantes.
 */
@Entity
@Table(name = "gastos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagador_id", nullable = false)
    private Usuario pagador;

    @Enumerated(EnumType.STRING)
    @Column(name = "regla_division", nullable = false)
    private ReglaDivision reglaDivision;

    @OneToMany(mappedBy = "gasto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GastoParticipante> participantes = new ArrayList<>();

    /**
     * Agrega un participante a la coleccion de participantes del gasto,
     * manteniendo la coherencia bidireccional de la relacion.
     *
     * @param participante Instancia de GastoParticipante a asociar.
     */
    public void agregarParticipante(GastoParticipante participante) {
        participantes.add(participante);
        participante.setGasto(this);
    }

    /**
     * Remueve un participante de la coleccion de participantes del gasto,
     * desvinculando la relacion bidireccional.
     *
     * @param participante Instancia de GastoParticipante a remover.
     */
    public void removerParticipante(GastoParticipante participante) {
        participantes.remove(participante);
        participante.setGasto(null);
    }
}
