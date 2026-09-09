package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la persistencia y consulta de plantillas de gastos recurrentes y favoritos.
 */
public interface PlantillaGastoRepository extends JpaRepository<PlantillaGastoRecurrente, Long> {

    /**
     * Recupera todas las plantillas asociadas a un espacio especifico.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de plantillas registradas en el espacio.
     */
    @Query("SELECT p FROM PlantillaGastoRecurrente p WHERE p.espacioId.id = :espacioId")
    List<PlantillaGastoRecurrente> findByEspacioId(@Param("espacioId") Long espacioId);
}