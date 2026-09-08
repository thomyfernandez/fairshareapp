package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.GastoParticipante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad GastoParticipante.
 * Provee metodos de consulta y persistencia para las particiones asignadas a los usuarios.
 */
public interface GastoParticipanteRepository extends JpaRepository<GastoParticipante, Long> {

    /**
     * Obtiene la lista de participaciones asociadas a un gasto determinado.
     *
     * @param gastoId Identificador del gasto.
     * @return Lista de participaciones del gasto.
     */
    List<GastoParticipante> findByGastoId(Long gastoId);

    /**
     * Obtiene la lista de participaciones de un usuario especifico.
     *
     * @param usuarioId Identificador del usuario participante.
     * @return Lista de participaciones en las que intervino el usuario.
     */
    List<GastoParticipante> findByUsuarioId(Long usuarioId);
}
