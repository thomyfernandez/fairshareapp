package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Liquidacion.
 * Provee operaciones de persistencia y busqueda de las liquidaciones historicas de un espacio.
 */
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {

    /**
     * Busca la liquidacion generada para un periodo especifico.
     *
     * @param periodoId Identificador unico del periodo.
     * @return Optional con la liquidacion si existe.
     */
    Optional<Liquidacion> findByPeriodoId(Long periodoId);

    /**
     * Verifica si ya existe una liquidacion generada para un periodo especifico.
     *
     * @param periodoId Identificador unico del periodo.
     * @return true si ya existe, false en caso contrario.
     */
    boolean existsByPeriodoId(Long periodoId);

    /**
     * Recupera las liquidaciones historicas de un espacio, ordenadas de la mas reciente a la mas antigua.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de liquidaciones del espacio.
     */
    List<Liquidacion> findByEspacioIdOrderByFechaGeneracionDesc(Long espacioId);
}
