package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Sueldo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SueldoRepository extends JpaRepository<Sueldo, Long> {

    /**
     * Busca todos los sueldos asociados a un usuario especifico.
     *
     * @param usuarioId Identificador unico del usuario.
     * @return Lista de sueldos pertenecientes al usuario.
     */
    List<Sueldo> findByUsuario_Id(Long usuarioId);
}