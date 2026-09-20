package com.example.fairshareapp.service.impl;

import com.example.fairshareapp.exception.MembresiaDuplicadaException;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarSueldoDTO;
import com.example.fairshareapp.model.dto.MiembroResponseDTO;
import com.example.fairshareapp.model.dto.UnirseEspacioDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.MiembroEspacio;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.RolMiembro;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.MiembroEspacioRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import com.example.fairshareapp.service.MiembroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementacion del servicio transaccional para la administracion de miembros de un espacio compartido,
 * incluyendo union mediante codigo de invitacion, asignacion de roles y gestion del sueldo declarado.
 */
@Service
@Transactional
public class MiembroServiceImpl implements MiembroService {

    private final MiembroEspacioRepository miembroEspacioRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor para la inyeccion de los repositorios de miembros, espacios y usuarios.
     *
     * @param miembroEspacioRepository Repositorio de datos para membresias.
     * @param espacioRepository Repositorio de datos para espacios.
     * @param usuarioRepository Repositorio de datos para usuarios.
     */
    public MiembroServiceImpl(MiembroEspacioRepository miembroEspacioRepository,
                               EspacioRepository espacioRepository,
                               UsuarioRepository usuarioRepository) {
        this.miembroEspacioRepository = miembroEspacioRepository;
        this.espacioRepository = espacioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Une a un usuario a un espacio validando que el codigo provisto coincida con el del espacio
     * y que el usuario no sea ya miembro. El primer miembro del espacio se asigna como ADMIN.
     *
     * @param espacioId Identificador del espacio al que se desea unir.
     * @param unirseDTO Datos de union con codigo, usuario y sueldo declarado opcional.
     * @return MiembroResponseDTO con el detalle de la membresia creada.
     */
    @Override
    public MiembroResponseDTO unirseAEspacio(Long espacioId, UnirseEspacioDTO unirseDTO) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        if (unirseDTO.getCodigo() == null || !unirseDTO.getCodigo().trim().equalsIgnoreCase(espacio.getCodigo())) {
            throw new ReglaInvalidaException("El codigo de invitacion no coincide con el espacio indicado");
        }

        Usuario usuario = usuarioRepository.findById(unirseDTO.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + unirseDTO.getUsuarioId()));

        if (miembroEspacioRepository.existsByEspacioIdAndUsuarioId(espacioId, usuario.getId())) {
            throw new MembresiaDuplicadaException("El usuario con id " + usuario.getId() + " ya es miembro del espacio con id " + espacioId);
        }

        if (unirseDTO.getSueldoDeclarado() != null && unirseDTO.getSueldoDeclarado().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El sueldo declarado no puede ser negativo");
        }

        boolean esPrimerMiembro = miembroEspacioRepository.findByEspacioId(espacioId).isEmpty();

        MiembroEspacio miembro = MiembroEspacio.builder()
                .espacio(espacio)
                .usuario(usuario)
                .rol(esPrimerMiembro ? RolMiembro.ADMIN : RolMiembro.MIEMBRO)
                .sueldoDeclarado(unirseDTO.getSueldoDeclarado())
                .build();

        MiembroEspacio guardado = miembroEspacioRepository.save(miembro);
        return mapToResponseDTO(guardado);
    }

    /**
     * Une a un usuario a un espacio resolviendo el destino unicamente a traves del codigo de invitacion.
     *
     * @param unirseDTO Datos de union conteniendo codigo de invitacion, usuario y sueldo opcional.
     * @return MiembroResponseDTO con el detalle de la membresia registrada.
     */
    @Override
    public MiembroResponseDTO unirsePorCodigo(UnirseEspacioDTO unirseDTO) {
        if (unirseDTO.getCodigo() == null || unirseDTO.getCodigo().trim().isEmpty()) {
            throw new ReglaInvalidaException("El codigo de invitacion es obligatorio");
        }
        String codigoTrimmed = unirseDTO.getCodigo().trim();
        Espacio espacio = espacioRepository.findByCodigoIgnoreCase(codigoTrimmed)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con codigo: " + codigoTrimmed));
        return unirseAEspacio(espacio.getId(), unirseDTO);
    }

    /**
     * Obtiene el listado de miembros pertenecientes a un espacio, validando su existencia previa.
     *
     * @param espacioId Identificador del espacio.
     * @return Lista de MiembroResponseDTO con los miembros del espacio.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MiembroResponseDTO> listarMiembros(Long espacioId) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }
        return miembroEspacioRepository.findByEspacioId(espacioId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Asigna o modifica el rol de un miembro dentro de un espacio sin validacion de solicitante.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param rol Nuevo rol a asignar.
     * @return MiembroResponseDTO con el rol actualizado.
     */
    @Override
    public MiembroResponseDTO asignarRol(Long espacioId, Long usuarioId, RolMiembro rol) {
        return asignarRol(espacioId, usuarioId, rol, null);
    }

    /**
     * Asigna o modifica el rol de un miembro dentro de un espacio validando que el solicitante sea ADMIN.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param rol Nuevo rol a asignar.
     * @param solicitanteId Identificador del usuario solicitante con permisos administrativos.
     * @return MiembroResponseDTO con el rol actualizado.
     */
    @Override
    public MiembroResponseDTO asignarRol(Long espacioId, Long usuarioId, RolMiembro rol, Long solicitanteId) {
        if (rol == null) {
            throw new ReglaInvalidaException("El rol a asignar no puede ser nulo");
        }
        if (solicitanteId != null) {
            validarPermisoAdmin(espacioId, solicitanteId);
        }
        MiembroEspacio miembro = obtenerMembresia(espacioId, usuarioId);
        miembro.setRol(rol);
        MiembroEspacio guardado = miembroEspacioRepository.save(miembro);
        return mapToResponseDTO(guardado);
    }

    /**
     * Registra o actualiza el sueldo mensual declarado de un miembro dentro de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param actualizarSueldoDTO Datos con el nuevo sueldo declarado.
     * @return MiembroResponseDTO con el sueldo actualizado.
     */
    @Override
    public MiembroResponseDTO actualizarSueldo(Long espacioId, Long usuarioId, ActualizarSueldoDTO actualizarSueldoDTO) {
        BigDecimal sueldoDeclarado = actualizarSueldoDTO.getSueldoDeclarado();
        if (sueldoDeclarado == null || sueldoDeclarado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El sueldo declarado no puede ser nulo ni negativo");
        }
        MiembroEspacio miembro = obtenerMembresia(espacioId, usuarioId);
        miembro.setSueldoDeclarado(sueldoDeclarado);
        MiembroEspacio guardado = miembroEspacioRepository.save(miembro);
        return mapToResponseDTO(guardado);
    }

    /**
     * Busca la membresia de un usuario dentro de un espacio, lanzando una excepcion si no existe.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     * @return Entidad MiembroEspacio encontrada.
     */
    private MiembroEspacio obtenerMembresia(Long espacioId, Long usuarioId) {
        return miembroEspacioRepository.findByEspacioIdAndUsuarioId(espacioId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario con id " + usuarioId + " no es miembro del espacio con id " + espacioId));
    }

    /**
     * Mapea una entidad MiembroEspacio a su correspondiente DTO de respuesta MiembroResponseDTO.
     *
     * @param miembro Entidad MiembroEspacio a transformar.
     * @return DTO de respuesta con la informacion consolidada de la membresia.
     */
    private MiembroResponseDTO mapToResponseDTO(MiembroEspacio miembro) {
        Usuario usuario = miembro.getUsuario();
        String nombreCompleto = usuario != null
                ? String.join(" ", nonNullOrEmpty(usuario.getNombre()), nonNullOrEmpty(usuario.getApellido())).trim()
                : null;
        String nombreVisible = null;
        if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
            nombreVisible = nombreCompleto;
        } else if (usuario != null) {
            nombreVisible = usuario.getNombreUsuario();
        }

        return MiembroResponseDTO.builder()
                .id(miembro.getId())
                .espacioId(miembro.getEspacio() != null ? miembro.getEspacio().getId() : null)
                .usuarioId(usuario != null ? usuario.getId() : null)
                .nombreUsuario(nombreVisible)
                .emailUsuario(usuario != null ? usuario.getEmail() : null)
                .rol(miembro.getRol())
                .sueldoDeclarado(miembro.getSueldoDeclarado())
                .build();
    }

    /**
     * Valida que un usuario pertenezca a un espacio compartido, lanzando excepcion si no es miembro.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public void validarPertenencia(Long espacioId, Long usuarioId) {
        if (!miembroEspacioRepository.existsByEspacioIdAndUsuarioId(espacioId, usuarioId)) {
            throw new RecursoNoEncontradoException("El usuario con id " + usuarioId + " no es miembro del espacio con id " + espacioId);
        }
    }

    /**
     * Valida que un usuario pertenezca a un espacio y posea el rol de ADMIN.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public void validarPermisoAdmin(Long espacioId, Long usuarioId) {
        if (!miembroEspacioRepository.existsByEspacioIdAndUsuarioId(espacioId, usuarioId)) {
            throw new RecursoNoEncontradoException("El usuario con id " + usuarioId + " no es miembro del espacio con id " + espacioId);
        }
        if (!miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(espacioId, usuarioId, RolMiembro.ADMIN)) {
            throw new ReglaInvalidaException("Acceso denegado: se requieren permisos de administrador en el espacio");
        }
    }

    /**
     * Comprueba de forma booleana si un usuario es miembro de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     * @return true si es miembro activo del espacio.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean esMiembro(Long espacioId, Long usuarioId) {
        return miembroEspacioRepository.existsByEspacioIdAndUsuarioId(espacioId, usuarioId);
    }

    /**
     * Comprueba de forma booleana si un usuario es administrador de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario.
     * @return true si es administrador del espacio.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean esAdmin(Long espacioId, Long usuarioId) {
        return miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(espacioId, usuarioId, RolMiembro.ADMIN);
    }

    /**
     * Retorna el valor recibido o una cadena vacia en caso de ser nulo.
     *
     * @param valor Cadena original a evaluar.
     * @return Cadena garantizada como no nula.
     */
    private String nonNullOrEmpty(String valor) {
        return valor != null ? valor : "";
    }
}
