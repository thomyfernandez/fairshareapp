package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    default Optional<Usuario> getByEmail(String email) { return findByEmailIgnoreCase(email); }

}
