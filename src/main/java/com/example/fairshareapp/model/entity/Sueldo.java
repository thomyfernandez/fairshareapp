package com.example.fairshareapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;
import com.example.fairshareapp.model.response.SueldoResponse;

@Entity
@Table(name = "sueldos")
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

    public TipoSueldo getTipo() {
        return tipo;
    }

    public void setTipo(TipoSueldo tipo) {
        this.tipo = tipo;
    }

    public FrecuenciaSueldo getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(FrecuenciaSueldo frecuencia) {
        this.frecuencia = frecuencia;
    }

    // toSueldoResponse
    public SueldoResponse toSueldoResponse() {
        SueldoResponse sueldoResponse = new SueldoResponse();
        sueldoResponse.setId(this.getId());
        sueldoResponse.setMonto(this.getMonto());
        sueldoResponse.setTipo(this.getTipo());
        sueldoResponse.setFrecuencia(this.getFrecuencia());

        return sueldoResponse;

    }

}
