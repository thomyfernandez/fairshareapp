package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Sueldo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SueldoRepository extends JpaRepository<Sueldo, Long> {

    /**
     * Busca todos los sueldos asociados a un usuario especifico.
     *
     * @param usuarioId Identificador unico del usuario.
     * @return Lista de sueldos pertenecientes al usuario.
     */
    List<Sueldo> findByUsuario_Id(Long usuarioId);

    /**
     * Busca el sueldo registrado de un usuario para un anio y mes determinados.
     *
     * @param usuarioId Identificador unico del usuario.
     * @param anio Anio de vigencia del sueldo.
     * @param mes Mes de vigencia del sueldo (1 a 12).
     * @return Optional con el sueldo encontrado si existe.
     */
    Optional<Sueldo> findByUsuario_IdAndAnioAndMes(Long usuarioId, Integer anio, Integer mes);

    /**
     * Obtiene el ultimo sueldo registrado de un usuario ordenado por anio y mes descendente.
     *
     * @param usuarioId Identificador unico del usuario.
     * @return Optional con el sueldo mas reciente del usuario.
     */
    Optional<Sueldo> findFirstByUsuario_IdOrderByAnioDescMesDesc(Long usuarioId);
}