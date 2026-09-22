package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Espacio.
 * Provee operaciones de persistencia, busqueda y validacion de unicidad de nombres y codigos de espacios.
 */
@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Espacio e where e.id = :id")
    Optional<Espacio> findForUpdateById(@org.springframework.data.repository.query.Param("id") Long id);


    /**
     * Busca un espacio por su nombre exacto.
     *
     * @param nombre Nombre del espacio.
     * @return Optional con el espacio si existe.
     */
    Optional<Espacio> findByNombre(String nombre);

    /**
     * Busca un espacio por su codigo unico.
     *
     * @param codigo Codigo identificador del espacio.
     * @return Optional con el espacio si existe.
     */
    Optional<Espacio> findByCodigo(String codigo);

    /**
     * Verifica si ya existe un espacio con el nombre dado.
     *
     * @param nombre Nombre a verificar.
     * @return true si ya existe, false en caso contrario.
     */
    boolean existsByNombre(String nombre);

    /**
     * Verifica si ya existe un espacio con el codigo dado.
     *
     * @param codigo Codigo a verificar.
     * @return true si ya existe, false en caso contrario.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Verifica si existe otro espacio con el mismo nombre excluyendo un id especifico.
     *
     * @param nombre Nombre a verificar.
     * @param id Identificador a excluir de la busqueda.
     * @return true si existe colision con otro registro.
     */
    boolean existsByNombreAndIdNot(String nombre, Long id);

    /**
     * Verifica si existe otro espacio con el mismo codigo excluyendo un id especifico.
     *
     * @param codigo Codigo a verificar.
     * @param id Identificador a excluir de la busqueda.
     * @return true si existe colision con otro registro.
     */
    boolean existsByCodigoAndIdNot(String codigo, Long id);

    /**
     * Busca un espacio por su codigo unico ignorando mayusculas y minusculas.
     *
     * @param codigo Codigo identificador del espacio.
     * @return Optional con el espacio si coincide el codigo.
     */
    Optional<Espacio> findByCodigoIgnoreCase(String codigo);

    /**
     * Verifica si ya existe un espacio con el codigo dado ignorando mayusculas y minusculas.
     *
     * @param codigo Codigo a verificar.
     * @return true si ya existe un espacio con ese codigo.
     */
    boolean existsByCodigoIgnoreCase(String codigo);

    /**
     * Verifica si ya existe un espacio con el nombre dado ignorando mayusculas y minusculas.
     *
     * @param nombre Nombre a verificar.
     * @return true si ya existe un espacio con ese nombre.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe otro espacio con el mismo codigo excluyendo un id e ignorando mayusculas/minusculas.
     *
     * @param codigo Codigo a verificar.
     * @param id Identificador a excluir de la comprobacion.
     * @return true si colisiona con otro espacio.
     */
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    /**
     * Verifica si existe otro espacio con el mismo nombre excluyendo un id e ignorando mayusculas/minusculas.
     *
     * @param nombre Nombre a verificar.
     * @param id Identificador a excluir de la comprobacion.
     * @return true si colisiona con otro espacio.
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}

