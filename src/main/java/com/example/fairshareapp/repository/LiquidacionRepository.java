package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la gestion de entidades Liquidacion.
 * Ofrece consultas para recuperar historiales de liquidaciones por espacio, rango de fechas y periodo mensual.
 */
@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {

    /**
     * Recupera el historial completo de liquidaciones de un espacio ordenadas descendentemente por fecha.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de liquidaciones del espacio.
     */
    List<Liquidacion> findByEspacio_IdOrderByFechaLiquidacionDesc(Long espacioId);

    /**
     * Recupera las liquidaciones de un espacio comprendidas en un rango de fechas determinado.
     *
     * @param espacioId Identificador unico del espacio.
     * @param desde Fecha inicial inclusiva.
     * @param hasta Fecha final inclusiva.
     * @return Lista de liquidaciones que coinciden con el periodo.
     */
    List<Liquidacion> findByEspacio_IdAndFechaLiquidacionBetweenOrderByFechaLiquidacionDesc(Long espacioId, LocalDate desde, LocalDate hasta);

    /**
     * Recupera las liquidaciones de un espacio pertenecientes a un anio y mes especifico.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio del periodo.
     * @param mes Mes del periodo (1 a 12).
     * @return Lista de liquidaciones para dicho mes y anio.
     */
    List<Liquidacion> findByEspacio_IdAndAnioAndMesOrderByFechaLiquidacionDesc(Long espacioId, Integer anio, Integer mes);
}
