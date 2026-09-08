package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.response.SueldoResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SueldoRepository extends JpaRepository<Sueldo, Long> {
    // Permite buscar todos los sueldos de un usuario especifico
    List<Sueldo> findByUsuario_Id(Long usuarioId);

    // Buscar todos los sueldos
    List<Sueldo> findAll();
}