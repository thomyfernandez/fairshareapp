package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Periodo;
import com.example.fairshareapp.model.enums.EstadoPeriodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Periodo.
 * Provee operaciones de persistencia y busqueda de periodos mensuales de un espacio.
 */
public interface PeriodoRepository extends JpaRepository<Periodo, Long> {

    /**
     * Busca el periodo de un espacio correspondiente a un anio y mes especificos.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio del periodo.
     * @param mes Mes del periodo.
     * @return Optional con el periodo si existe.
     */
    Optional<Periodo> findByEspacioIdAndAnioAndMes(Long espacioId, Integer anio, Integer mes);

    /**
     * Busca el periodo de un espacio correspondiente a un anio y mes especificos, filtrando por estado.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio del periodo.
     * @param mes Mes del periodo.
     * @param estado Estado del periodo a filtrar.
     * @return Optional con el periodo si existe y coincide el estado.
     */
    Optional<Periodo> findByEspacioIdAndAnioAndMesAndEstado(Long espacioId, Integer anio, Integer mes, EstadoPeriodo estado);

    /**
     * Recupera todos los periodos de un espacio, ordenados del mas reciente al mas antiguo.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de periodos del espacio.
     */
    List<Periodo> findByEspacioIdOrderByAnioDescMesDesc(Long espacioId);

    /**
     * Verifica si ya existe un periodo cargado para un espacio en un anio y mes determinados.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio del periodo.
     * @param mes Mes del periodo.
     * @return true si ya existe, false en caso contrario.
     */
    boolean existsByEspacioIdAndAnioAndMes(Long espacioId, Integer anio, Integer mes);
}
