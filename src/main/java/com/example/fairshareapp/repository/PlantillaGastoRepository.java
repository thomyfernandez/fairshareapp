package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la persistencia y consulta de plantillas de gastos recurrentes y favoritos.
 */
@Repository
public interface PlantillaGastoRepository extends JpaRepository<PlantillaGastoRecurrente, Long> {

    /**
     * Recupera todas las plantillas asociadas a un espacio especifico.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de plantillas registradas en el espacio.
     */
    @Query("SELECT p FROM PlantillaGastoRecurrente p WHERE p.espacioId.id = :espacioId")
    List<PlantillaGastoRecurrente> findByEspacioId(@Param("espacioId") Long espacioId);

    /**
     * Recupera las plantillas de un espacio cuyo ciclo de revision ya se encuentra vencido a una fecha dada.
     *
     * @param espacioId Identificador unico del espacio.
     * @param fecha Fecha contra la cual se evalua el vencimiento (habitualmente hoy).
     * @return Lista de plantillas del espacio con fechaProximaRevision menor o igual a la fecha indicada.
     */
    @Query("SELECT p FROM PlantillaGastoRecurrente p WHERE p.espacioId.id = :espacioId "
            + "AND p.fechaProximaRevision IS NOT NULL AND p.fechaProximaRevision <= :fecha")
    List<PlantillaGastoRecurrente> findVencidasPorEspacio(@Param("espacioId") Long espacioId, @Param("fecha") LocalDate fecha);
}