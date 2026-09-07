package com.example.fairshareapp.model.entity;

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
    private Double monto;

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

    /**
     * Obtiene el identificador unico del gasto.
     *
     * @return Identificador del gasto.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el identificador unico del gasto.
     *
     * @param id Identificador a asignar.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene la descripcion o concepto del gasto.
     *
     * @return Descripcion del gasto.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripcion o concepto del gasto.
     *
     * @param descripcion Descripcion a asignar.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el monto total del gasto.
     *
     * @return Monto numerico del gasto.
     */
    public Double getMonto() {
        return monto;
    }

    /**
     * Establece el monto total del gasto.
     *
     * @param monto Monto a asignar.
     */
    public void setMonto(Double monto) {
        this.monto = monto;
    }

    /**
     * Obtiene la fecha en la que se registro el gasto.
     *
     * @return Fecha del gasto.
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha del gasto.
     *
     * @param fecha Fecha a asignar.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el espacio compartido al que pertenece el gasto.
     *
     * @return Espacio asociado.
     */
    public Espacio getEspacio() {
        return espacio;
    }

    /**
     * Establece el espacio compartido al que pertenece el gasto.
     *
     * @param espacio Espacio a asociar.
     */
    public void setEspacio(Espacio espacio) {
        this.espacio = espacio;
    }

    /**
     * Obtiene la categoria asignada al gasto.
     *
     * @return Categoria asociada.
     */
    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Establece la categoria del gasto.
     *
     * @param categoria Categoria a asociar.
     */
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /**
     * Obtiene el usuario que realizo el pago del gasto.
     *
     * @return Instancia del usuario pagador.
     */
    public Usuario getPagador() {
        return pagador;
    }

    /**
     * Establece el usuario que realizo el pago del gasto.
     *
     * @param pagador Usuario pagador a asociar.
     */
    public void setPagador(Usuario pagador) {
        this.pagador = pagador;
    }

    /**
     * Obtiene la regla de division aplicada al gasto.
     *
     * @return Regla de division configurada.
     */
    public ReglaDivision getReglaDivision() {
        return reglaDivision;
    }

    /**
     * Establece la regla de division para el gasto.
     *
     * @param reglaDivision Regla de division a aplicar.
     */
    public void setReglaDivision(ReglaDivision reglaDivision) {
        this.reglaDivision = reglaDivision;
    }

    /**
     * Obtiene la lista de participantes vinculados al gasto.
     *
     * @return Coleccion de participantes del gasto.
     */
    public List<GastoParticipante> getParticipantes() {
        return participantes;
    }

    /**
     * Establece la lista de participantes vinculados al gasto.
     *
     * @param participantes Coleccion de participantes a asignar.
     */
    public void setParticipantes(List<GastoParticipante> participantes) {
        this.participantes = participantes;
    }
}
