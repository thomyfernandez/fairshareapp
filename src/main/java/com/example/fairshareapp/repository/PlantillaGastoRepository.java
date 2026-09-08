package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaGastoRepository extends JpaRepository<PlantillaGastoRecurrente, Long> {
    

    List<PlantillaGastoRecurrente> findByEspacioId(Long espacioId);
}