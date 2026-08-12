package com.example.fairshareapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Entidad JPA que representa un gasto dentro de la aplicacion.
 * Mapea los atributos de un gasto con la tabla 'gastos' en la base de datos.
 */
@Entity
@Table(name = "gastos")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false)
    private String pagador;

    @Column(name = "fecha")
    private LocalDate fecha;

    /**
     * Constructor vacio requerido por la especificacion JPA.
     */
    public Gasto() {
    }

    /**
     * Constructor con parametros para inicializar una instancia de Gasto.
     * 
     * @param descripcion Detalle o concepto del gasto.
     * @param monto Valor monetario del gasto.
     * @param pagador Nombre o identificador del usuario que pago el gasto.
     * @param fecha Fecha en la que se efectuo el gasto.
     */
    public Gasto(String descripcion, Double monto, String pagador, LocalDate fecha) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.pagador = pagador;
        this.fecha = fecha;
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
     * Obtiene la descripcion del gasto.
     * 
     * @return Descripcion del gasto.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripcion del gasto.
     * 
     * @param descripcion Descripcion a asignar.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el monto total del gasto.
     * 
     * @return Monto del gasto.
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
     * Obtiene la persona que realizo el pago.
     * 
     * @return Nombre o identificador del pagador.
     */
    public String getPagador() {
        return pagador;
    }

    /**
     * Establece la persona que realizo el pago.
     * 
     * @param pagador Nombre o identificador del pagador.
     */
    public void setPagador(String pagador) {
        this.pagador = pagador;
    }

    /**
     * Obtiene la fecha del gasto.
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
}
