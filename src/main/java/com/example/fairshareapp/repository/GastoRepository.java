package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Spring Data JPA para la entidad Gasto.
 * Proporciona metodos de acceso a datos y operaciones CRUD en la base de datos.
 */
public interface GastoRepository extends JpaRepository<Gasto, Long> {
}

