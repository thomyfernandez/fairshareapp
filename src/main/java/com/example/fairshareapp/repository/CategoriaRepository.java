package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Categoria.
 * Provee operaciones de persistencia y busqueda de categorias de gastos.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoria por su nombre exacto.
     *
     * @param nombre Nombre de la categoria a buscar.
     * @return Optional con la Categoria encontrada o vacio si no existe.
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
