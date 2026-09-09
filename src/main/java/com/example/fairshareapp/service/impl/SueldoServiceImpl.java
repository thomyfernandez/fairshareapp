package com.example.fairshareapp.service.impl;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.mapper.SueldoMapper;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.repository.SueldoRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import com.example.fairshareapp.service.SueldoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Implementacion de logica de negocio para la gestion de sueldos de usuarios.
 */
@Service
@Transactional
public class SueldoServiceImpl implements SueldoService {

    private static final String SUELDO_NO_ENCONTRADO = "Sueldo no encontrado con id: ";

    private final SueldoRepository sueldoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SueldoMapper sueldoMapper;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param sueldoRepository Repositorio de sueldos.
     * @param usuarioRepository Repositorio de usuarios.
     * @param sueldoMapper Mapeador de sueldos y DTOs.
     */
    public SueldoServiceImpl(SueldoRepository sueldoRepository,
                             UsuarioRepository usuarioRepository,
                             SueldoMapper sueldoMapper) {
        this.sueldoRepository = sueldoRepository;
        this.usuarioRepository = usuarioRepository;
        this.sueldoMapper = sueldoMapper;
    }

    /**
     * Registra un nuevo sueldo asociado a un usuario y sincroniza su sueldo actual.
     *
     * @param usuarioId Identificador del usuario.
     * @param sueldoRequest Datos del sueldo a crear.
     * @return SueldoResponse con la informacion registrada.
     */
    @Override
    public SueldoResponse crearSueldo(Long usuarioId, SueldoRequest sueldoRequest) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId));

        Sueldo sueldo = Sueldo.builder()
                .monto(sueldoRequest.getMonto())
                .tipo(sueldoRequest.getTipo())
                .frecuencia(sueldoRequest.getFrecuencia())
                .usuario(usuario)
                .fechaInicio(LocalDate.now(ZoneId.systemDefault()))
                .build();

        // Sincroniza el sueldo en el usuario para calculos proporcionales
        if (sueldoRequest.getMonto() != null) {
            usuario.setSueldo(sueldoRequest.getMonto());
            usuarioRepository.save(usuario);
        }

        sueldoRepository.save(sueldo);
        return sueldoMapper.toResponse(sueldo);
    }

    /**
     * Guarda una copia de una entidad de sueldo existente.
     *
     * @param copia Entidad de sueldo a persistir.
     * @return SueldoResponse con los datos persistidos.
     */
    @Override
    public SueldoResponse crearSueldoCopia(Sueldo copia) {
        sueldoRepository.save(copia);
        return sueldoMapper.toResponse(copia);
    }

    /**
     * Obtiene el detalle de un sueldo por su identificador.
     *
     * @param id Identificador unico del sueldo.
     * @return SueldoResponse encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public SueldoResponse obtenerSueldo(Long id) {
        Sueldo sueldo = sueldoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(SUELDO_NO_ENCONTRADO + id));
        return sueldoMapper.toResponse(sueldo);
    }

    /**
     * Retorna los sueldos historicos o registrados de un usuario especifico.
     *
     * @param usuarioId Identificador del usuario.
     * @return Lista de SueldoResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SueldoResponse> obtenerSueldosPorUsuario(Long usuarioId) {
        return sueldoMapper.toResponseList(sueldoRepository.findByUsuario_Id(usuarioId));
    }

    /**
     * Actualiza el monto, tipo y frecuencia de un sueldo existente.
     *
     * @param id Identificador unico del sueldo.
     * @param sueldoRequest Nuevos datos del sueldo.
     */
    @Override
    public void actualizarSueldo(Long id, SueldoRequest sueldoRequest) {
        Sueldo sueldo = sueldoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(SUELDO_NO_ENCONTRADO + id));

        sueldo.setMonto(sueldoRequest.getMonto());
        sueldo.setTipo(sueldoRequest.getTipo());
        sueldo.setFrecuencia(sueldoRequest.getFrecuencia());

        if (sueldo.getUsuario() != null && sueldoRequest.getMonto() != null) {
            Usuario usuario = sueldo.getUsuario();
            usuario.setSueldo(sueldoRequest.getMonto());
            usuarioRepository.save(usuario);
        }

        sueldoRepository.save(sueldo);
    }

    /**
     * Elimina un sueldo por su identificador.
     *
     * @param id Identificador unico del sueldo.
     */
    @Override
    public void eliminarSueldo(Long id) {
        if (!sueldoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(SUELDO_NO_ENCONTRADO + id);
        }
        sueldoRepository.deleteById(id);
    }

    /**
     * Retorna todos los sueldos registrados en el sistema.
     *
     * @return Lista con todos los SueldoResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SueldoResponse> getAllSueldos() {
        return sueldoMapper.toResponseList(sueldoRepository.findAll());
    }

    /**
     * Metodo alternativo de borrado de sueldo.
     *
     * @param id Identificador unico del sueldo.
     */
    @Override
    public void deleteSueldo(Long id) {
        eliminarSueldo(id);
    }

    /**
     * Actualiza la entidad sueldo a partir de un SueldoResponse.
     *
     * @param id Identificador unico del sueldo.
     * @param sueldo Datos recibidos a actualizar.
     * @return Entidad Sueldo actualizada.
     */
    @Override
    public Sueldo updateSueldo(Long id, SueldoResponse sueldo) {
        Sueldo sueldoFinded = sueldoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(SUELDO_NO_ENCONTRADO + id));

        if (sueldo.getMonto() != null) {
            sueldoFinded.setMonto(sueldo.getMonto());
        }
        if (sueldo.getTipo() != null) {
            sueldoFinded.setTipo(sueldo.getTipo());
        }
        if (sueldo.getFrecuencia() != null) {
            sueldoFinded.setFrecuencia(sueldo.getFrecuencia());
        }

        return sueldoRepository.save(sueldoFinded);
    }
}
