package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la persistencia y gestion del catalogo de servicios.
 */
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /**
     * Recupera el listado de servicios actualmente activos.
     *
     * @return Lista de servicios activos.
     */
    List<Servicio> findByActivoTrue();
}