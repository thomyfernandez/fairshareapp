package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.dto.PresupuestoResponseDTO;
import com.example.fairshareapp.model.dto.PresupuestoUpsertDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Presupuesto;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.PresupuestoRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la administracion del presupuesto mensual de cada usuario dentro de un espacio.
 * Permite fijar (crear o actualizar) el limite de gasto mensual por usuario y consultarlo,
 * insumo utilizado por LiquidacionService para detectar excedentes al cerrar un periodo.
 */
@Service
@Transactional
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyeccion de dependencias de los repositorios requeridos.
     *
     * @param presupuestoRepository Repositorio de persistencia de presupuestos.
     * @param espacioRepository Repositorio de persistencia de espacios.
     * @param usuarioRepository Repositorio de persistencia de usuarios.
     */
    public PresupuestoService(PresupuestoRepository presupuestoRepository,
                               EspacioRepository espacioRepository,
                               UsuarioRepository usuarioRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.espacioRepository = espacioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea o actualiza (upsert) el presupuesto mensual de un usuario dentro de un espacio.
     *
     * @param espacioId Identificador unico del espacio.
     * @param usuarioId Identificador unico del usuario.
     * @param anio Anio del presupuesto.
     * @param mes Mes del presupuesto.
     * @param dto Datos del presupuesto a fijar.
     * @return Detalle del presupuesto creado o actualizado.
     */
    public PresupuestoResponseDTO fijarPresupuesto(Long espacioId, Long usuarioId, Integer anio, Integer mes, PresupuestoUpsertDTO dto) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId));

        Presupuesto presupuesto = presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(espacioId, usuarioId, anio, mes)
                .orElseGet(() -> Presupuesto.builder()
                        .espacio(espacio)
                        .usuario(usuario)
                        .anio(anio)
                        .mes(mes)
                        .build());

        presupuesto.setMontoLimite(dto.getMontoLimite());

        Presupuesto guardado = presupuestoRepository.save(presupuesto);
        return mapearADetalleDTO(guardado);
    }

    /**
     * Lista los presupuestos cargados en un espacio, filtrando opcionalmente por anio y mes.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio a filtrar (opcional).
     * @param mes Mes a filtrar (opcional).
     * @return Lista de presupuestos que coinciden con el filtro.
     */
    @Transactional(readOnly = true)
    public List<PresupuestoResponseDTO> listarPresupuestos(Long espacioId, Integer anio, Integer mes) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }

        List<Presupuesto> presupuestos = (anio != null && mes != null)
                ? presupuestoRepository.findByEspacioIdAndAnioAndMes(espacioId, anio, mes)
                : presupuestoRepository.findByEspacioId(espacioId);

        return presupuestos.stream().map(this::mapearADetalleDTO).toList();
    }

    /**
     * Busca el presupuesto de un usuario especifico para un periodo determinado.
     * Utilizado internamente por LiquidacionService al calcular excedentes de gasto.
     *
     * @param espacioId Identificador unico del espacio.
     * @param usuarioId Identificador unico del usuario.
     * @param anio Anio del periodo.
     * @param mes Mes del periodo.
     * @return Optional con el presupuesto si el usuario tiene uno cargado para ese periodo.
     */
    @Transactional(readOnly = true)
    public Optional<Presupuesto> buscarPresupuesto(Long espacioId, Long usuarioId, Integer anio, Integer mes) {
        return presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(espacioId, usuarioId, anio, mes);
    }

    /**
     * Mapea una entidad Presupuesto a su correspondiente representacion DTO.
     *
     * @param presupuesto Entidad de presupuesto a transformar.
     * @return DTO con el detalle del presupuesto.
     */
    private PresupuestoResponseDTO mapearADetalleDTO(Presupuesto presupuesto) {
        return PresupuestoResponseDTO.builder()
                .id(presupuesto.getId())
                .espacioId(presupuesto.getEspacio().getId())
                .usuarioId(presupuesto.getUsuario().getId())
                .usuarioNombre(presupuesto.getUsuario().getNombre() + " " + presupuesto.getUsuario().getApellido())
                .anio(presupuesto.getAnio())
                .mes(presupuesto.getMes())
                .montoLimite(presupuesto.getMontoLimite())
                .build();
    }
}
