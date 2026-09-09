package com.example.fairshareapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;
import com.example.fairshareapp.model.response.SueldoResponse;

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

    /**
     * Convierte la entidad Sueldo a su representacion SueldoResponse.
     *
     * @return DTO SueldoResponse con los datos de este registro.
     */
    public SueldoResponse toSueldoResponse() {
        SueldoResponse sueldoResponse = new SueldoResponse();
        sueldoResponse.setId(this.getId());
        sueldoResponse.setMonto(this.getMonto());
        sueldoResponse.setTipo(this.getTipo());
        sueldoResponse.setFrecuencia(this.getFrecuencia());
        sueldoResponse.setMes(this.getMes());
        sueldoResponse.setAnio(this.getAnio());
        if (this.getUsuario() != null) {
            sueldoResponse.setUsuarioId(this.getUsuario().getId());
            sueldoResponse.setUsuarioNombre(this.getUsuario().getNombre());
        }

        return sueldoResponse;
    }
}
