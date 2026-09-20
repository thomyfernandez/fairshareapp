package com.example.fairshareapp.service;

import com.example.fairshareapp.model.dto.ActualizarSueldoDTO;
import com.example.fairshareapp.model.dto.MiembroResponseDTO;
import com.example.fairshareapp.model.dto.UnirseEspacioDTO;
import com.example.fairshareapp.model.enums.RolMiembro;

import java.util.List;

/**
 * Interfaz de servicio que define los casos de uso para la administracion de miembros de un espacio,
 * incluyendo union mediante codigo de invitacion, asignacion de roles y gestion del sueldo declarado.
 */
public interface MiembroService {

    /**
     * Une a un usuario a un espacio compartido validando el codigo de invitacion e identificador de espacio.
     *
     * @param espacioId Identificador del espacio al que se desea unir.
     * @param unirseDTO Datos de union, incluyendo codigo, usuario y sueldo declarado opcional.
     * @return MiembroResponseDTO con el detalle de la membresia creada.
     */
    MiembroResponseDTO unirseAEspacio(Long espacioId, UnirseEspacioDTO unirseDTO);

    /**
     * Une a un usuario a un espacio compartido utilizando exclusivamente el codigo de invitacion.
     *
     * @param unirseDTO Datos de union conteniendo el codigo del espacio y el usuario.
     * @return MiembroResponseDTO con el detalle de la membresia creada.
     */
    MiembroResponseDTO unirsePorCodigo(UnirseEspacioDTO unirseDTO);

    /**
     * Obtiene el listado de miembros pertenecientes a un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @return Lista de MiembroResponseDTO con los miembros del espacio.
     */
    List<MiembroResponseDTO> listarMiembros(Long espacioId);

    /**
     * Asigna o modifica el rol de un miembro dentro de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param rol Nuevo rol a asignar.
     * @return MiembroResponseDTO con el rol actualizado.
     */
    MiembroResponseDTO asignarRol(Long espacioId, Long usuarioId, RolMiembro rol);

    /**
     * Asigna o modifica el rol de un miembro dentro de un espacio validando permisos del solicitante.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param rol Nuevo rol a asignar.
     * @param solicitanteId Identificador del usuario que solicita la modificacion (debe ser ADMIN).
     * @return MiembroResponseDTO con el rol actualizado.
     */
    MiembroResponseDTO asignarRol(Long espacioId, Long usuarioId, RolMiembro rol, Long solicitanteId);

    /**
     * Registra o actualiza el sueldo mensual declarado de un miembro dentro de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param actualizarSueldoDTO Datos con el nuevo sueldo declarado.
     * @return MiembroResponseDTO con el sueldo actualizado.
     */
    MiembroResponseDTO actualizarSueldo(Long espacioId, Long usuarioId, ActualizarSueldoDTO actualizarSueldoDTO);

    /**
     * Valida que un usuario pertenezca a un espacio compartido, lanzando excepcion si no es miembro.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     */
    void validarPertenencia(Long espacioId, Long usuarioId);

    /**
     * Valida que un usuario pertenezca a un espacio y posea el rol de ADMIN.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     */
    void validarPermisoAdmin(Long espacioId, Long usuarioId);

    /**
     * Comprueba de forma booleana si un usuario es miembro de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     * @return true si es miembro activo del espacio.
     */
    boolean esMiembro(Long espacioId, Long usuarioId);

    /**
     * Comprueba de forma booleana si un usuario es administrador de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     * @return true si es administrador del espacio.
     */
    boolean esAdmin(Long espacioId, Long usuarioId);
}
