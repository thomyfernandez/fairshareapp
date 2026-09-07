package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> getByEmail(String email);
}
