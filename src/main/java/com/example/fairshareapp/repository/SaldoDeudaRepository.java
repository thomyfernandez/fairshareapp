package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.enums.EstadoDeuda;
import com.example.fairshareapp.model.entity.SaldoDeuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad SaldoDeuda.
 * Provee metodos de consulta para la matriz de deudas de un espacio.
 */
@Repository
public interface SaldoDeudaRepository extends JpaRepository<SaldoDeuda, Long> {

    /**
     * Recupera las deudas de un espacio filtradas por estado.
     *
     * @param espacioId Identificador unico del espacio.
     * @param estado Estado de deuda a filtrar.
     * @return Lista de deudas que coinciden con el criterio de busqueda.
     */
    List<SaldoDeuda> findByEspacioIdAndEstado(Long espacioId, EstadoDeuda estado);
}
