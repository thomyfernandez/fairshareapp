package com.example.fairshareapp.service.impl;

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
import java.util.stream.Collectors;

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
            throw new ReglaInvalidaException("El usuario ya pertenece al espacio indicado");
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
                .collect(Collectors.toList());
    }

    /**
     * Asigna o modifica el rol de un miembro dentro de un espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param rol Nuevo rol a asignar.
     * @return MiembroResponseDTO con el rol actualizado.
     */
    @Override
    public MiembroResponseDTO asignarRol(Long espacioId, Long usuarioId, RolMiembro rol) {
        if (rol == null) {
            throw new ReglaInvalidaException("El rol a asignar no puede ser nulo");
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
        return MiembroResponseDTO.builder()
                .id(miembro.getId())
                .espacioId(miembro.getEspacio() != null ? miembro.getEspacio().getId() : null)
                .usuarioId(usuario != null ? usuario.getId() : null)
                .nombreUsuario(nombreCompleto != null && !nombreCompleto.isEmpty() ? nombreCompleto : (usuario != null ? usuario.getUsuario() : null))
                .emailUsuario(usuario != null ? usuario.getEmail() : null)
                .rol(miembro.getRol())
                .sueldoDeclarado(miembro.getSueldoDeclarado())
                .build();
    }

    private String nonNullOrEmpty(String valor) {
        return valor != null ? valor : "";
    }
}
