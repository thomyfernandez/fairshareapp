package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Espacio.
 * Provee operaciones de persistencia y recuperacion de espacios en la base de datos.
 */
@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Long> {
}
