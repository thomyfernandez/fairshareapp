package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.enums.EstadoGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Gasto.
 * Proporciona metodos de acceso a datos, filtrado por espacio, estado de liquidacion y periodos de tiempo.
 */
@Repository
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

    /**
     * Recupera los gastos de un espacio que se encuentran en un estado especifico (ej. PENDIENTE o LIQUIDADO).
     *
     * @param espacioId Identificador unico del espacio.
     * @param estado Estado de liquidacion de los gastos.
     * @return Lista de gastos coincidentes.
     */
    List<Gasto> findByEspacioIdAndEstadoOrderByFechaAsc(Long espacioId, EstadoGasto estado);

    /**
     * Recupera un conjunto de gastos por sus identificadores asegurando que pertenezcan al espacio indicado.
     *
     * @param ids Lista de identificadores de gastos.
     * @param espacioId Identificador unico del espacio.
     * @return Lista de gastos pertenecientes a dicho espacio.
     */
    List<Gasto> findByIdInAndEspacioId(List<Long> ids, Long espacioId);
}
