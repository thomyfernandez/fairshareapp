package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Presupuesto.
 * Provee operaciones de persistencia y busqueda de presupuestos mensuales por usuario y espacio.
 */
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    /**
     * Busca el presupuesto de un usuario en un espacio para un anio y mes especificos.
     *
     * @param espacioId Identificador unico del espacio.
     * @param usuarioId Identificador unico del usuario.
     * @param anio Anio del presupuesto.
     * @param mes Mes del presupuesto.
     * @return Optional con el presupuesto si existe.
     */
    Optional<Presupuesto> findByEspacioIdAndUsuarioIdAndAnioAndMes(Long espacioId, Long usuarioId, Integer anio, Integer mes);

    /**
     * Recupera los presupuestos cargados en un espacio para un anio y mes especificos.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio del presupuesto.
     * @param mes Mes del presupuesto.
     * @return Lista de presupuestos que coinciden con el periodo indicado.
     */
    List<Presupuesto> findByEspacioIdAndAnioAndMes(Long espacioId, Integer anio, Integer mes);

    /**
     * Recupera todos los presupuestos cargados en un espacio, sin importar el periodo.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de presupuestos del espacio.
     */
    List<Presupuesto> findByEspacioId(Long espacioId);
}
