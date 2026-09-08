package com.example.fairshareapp.model.entity; // <-- Fijate que ahora termina en .entity

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "plantillas_gastos_recurrentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaGastoRecurrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Integer frecuenciaAjusteMeses;
    private BigDecimal montoBase;
    private BigDecimal montoVariable;
    private LocalDate fechaProximaRevision;
    private Long espacioId; 
}