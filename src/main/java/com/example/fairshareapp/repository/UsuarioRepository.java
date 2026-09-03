package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Spring Data JPA para la entidad Usuario.
 * Proporciona metodos de acceso a datos y operaciones CRUD en la base de datos.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);
}
