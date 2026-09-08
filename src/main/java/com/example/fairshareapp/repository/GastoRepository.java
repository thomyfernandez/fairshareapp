package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Gasto.
 * Proporciona metodos de acceso a datos, filtrado por espacio y periodos de tiempo.
 */
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    /**
     * Recupera todos los gastos pertenecientes a un espacio ordenados descendentemente por fecha.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de gastos del espacio.
     */
    List<Gasto> findByEspacioIdOrderByFechaDesc(Long espacioId);

    /**
     * Recupera el lote de gastos de un espacio comprendidos dentro de un rango de fechas.
     *
     * @param espacioId Identificador unico del espacio.
     * @param fechaDesde Fecha inicial del periodo inclusiva.
     * @param fechaHasta Fecha final del periodo inclusiva.
     * @return Lista de gastos que coinciden con el criterio de busqueda.
     */
    List<Gasto> findByEspacioIdAndFechaBetweenOrderByFechaDesc(Long espacioId, LocalDate fechaDesde, LocalDate fechaHasta);
}
