package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.MiembroEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad MiembroEspacio.
 * Provee consultas de membresia por espacio y por usuario.
 */
public interface MiembroEspacioRepository extends JpaRepository<MiembroEspacio, Long> {

    /**
     * Obtiene la lista de miembros pertenecientes a un espacio determinado.
     *
     * @param espacioId Identificador del espacio.
     * @return Lista de membresias asociadas al espacio.
     */
    List<MiembroEspacio> findByEspacioId(Long espacioId);

    /**
     * Obtiene la lista de espacios a los que pertenece un usuario determinado.
     *
     * @param usuarioId Identificador del usuario.
     * @return Lista de membresias asociadas al usuario.
     */
    List<MiembroEspacio> findByUsuarioId(Long usuarioId);

    /**
     * Busca la membresia especifica de un usuario dentro de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     * @return Optional con la membresia si existe.
     */
    Optional<MiembroEspacio> findByEspacioIdAndUsuarioId(Long espacioId, Long usuarioId);

    /**
     * Verifica si un usuario ya pertenece a un espacio determinado.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     * @return true si la membresia ya existe, false en caso contrario.
     */
    boolean existsByEspacioIdAndUsuarioId(Long espacioId, Long usuarioId);
}
