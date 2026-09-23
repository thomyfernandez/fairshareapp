package com.example.fairshareapp.service.impl;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.exception.SueldoDuplicadoException;
import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.mapper.SueldoMapper;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.model.response.SueldoUpsertResult;
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

    /**
     * Anio minimo aceptado para un periodo de sueldo. Los registros anteriores al
     * año 2000 no se consideran validos para el sistema.
     */
    private static final int ANIO_MINIMO_ACEPTADO = 2000;

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
     * Registra un nuevo sueldo asociado a un usuario y periodo, o actualiza el ya existente
     * para esa misma combinacion de usuario/anio/mes, y sincroniza el sueldo vigente del usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @param sueldoRequest Datos del sueldo a crear o actualizar.
     * @return Resultado con el SueldoResponse y si el registro fue creado o actualizado.
     */
    @Override
    public SueldoUpsertResult crearOActualizarSueldo(Long usuarioId, SueldoRequest sueldoRequest) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId));

        LocalDate ahora = LocalDate.now(ZoneId.systemDefault());
        int mes = sueldoRequest.getMes() != null ? sueldoRequest.getMes() : ahora.getMonthValue();
        int anio = sueldoRequest.getAnio() != null ? sueldoRequest.getAnio() : ahora.getYear();
        validarPeriodo(mes, anio);

        var existente = sueldoRepository.findByUsuario_IdAndAnioAndMes(usuarioId, anio, mes);
        boolean creado = existente.isEmpty();

        Sueldo sueldo = existente.orElseGet(() -> Sueldo.builder()
                .usuario(usuario)
                .fechaInicio(ahora)
                .build());

        sueldo.setMonto(sueldoRequest.getMonto());
        sueldo.setTipo(sueldoRequest.getTipo());
        sueldo.setFrecuencia(sueldoRequest.getFrecuencia());
        sueldo.setMes(mes);
        sueldo.setAnio(anio);

        Sueldo guardado = sueldoRepository.save(sueldo);
        sincronizarSueldoVigente(usuario);

        return new SueldoUpsertResult(sueldoMapper.toResponse(guardado), creado);
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
        return sueldoMapper.toResponse(buscarSueldoOExplotar(id));
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
     * Retorna todos los sueldos registrados en el sistema.
     *
     * @return Lista con todos los SueldoResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SueldoResponse> obtenerTodos() {
        return sueldoMapper.toResponseList(sueldoRepository.findAll());
    }

    /**
     * Actualiza el monto, tipo, frecuencia y/o periodo de un sueldo existente.
     * El usuario propietario del sueldo no puede reasignarse mediante esta operacion.
     *
     * @param id Identificador unico del sueldo.
     * @param sueldoRequest Nuevos datos del sueldo.
     * @return SueldoResponse actualizado.
     */
    @Override
    public SueldoResponse actualizarSueldo(Long id, SueldoRequest sueldoRequest) {
        Sueldo sueldo = buscarSueldoOExplotar(id);

        int mes = sueldoRequest.getMes() != null ? sueldoRequest.getMes() : sueldo.getMes();
        int anio = sueldoRequest.getAnio() != null ? sueldoRequest.getAnio() : sueldo.getAnio();
        validarPeriodo(mes, anio);

        if (mes != sueldo.getMes() || anio != sueldo.getAnio()) {
            sueldoRepository.findByUsuario_IdAndAnioAndMes(sueldo.getUsuario().getId(), anio, mes)
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new SueldoDuplicadoException("Ya existe un sueldo registrado para el usuario "
                                + sueldo.getUsuario().getId() + " en el periodo " + mes + "/" + anio);
                    });
        }

        sueldo.setMonto(sueldoRequest.getMonto());
        sueldo.setTipo(sueldoRequest.getTipo());
        sueldo.setFrecuencia(sueldoRequest.getFrecuencia());
        sueldo.setMes(mes);
        sueldo.setAnio(anio);

        Sueldo guardado = sueldoRepository.save(sueldo);
        sincronizarSueldoVigente(sueldo.getUsuario());

        return sueldoMapper.toResponse(guardado);
    }

    /**
     * Elimina un sueldo por su identificador y resincroniza el sueldo vigente del usuario.
     *
     * @param id Identificador unico del sueldo.
     */
    @Override
    public void eliminarSueldo(Long id) {
        Sueldo sueldo = buscarSueldoOExplotar(id);
        Usuario usuario = sueldo.getUsuario();
        sueldoRepository.delete(sueldo);
        if (usuario != null) {
            sincronizarSueldoVigente(usuario);
        }
    }

    private Sueldo buscarSueldoOExplotar(Long id) {
        return sueldoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(SUELDO_NO_ENCONTRADO + id));
    }

    /**
     * Valida que el mes se encuentre entre 1 y 12, y que el anio este dentro del rango aceptado
     * (entre {@value #ANIO_MINIMO_ACEPTADO} y el anio actual mas uno, para permitir cargar
     * el sueldo del proximo periodo con antelacion).
     *
     * @param mes Mes del periodo a validar.
     * @param anio Anio del periodo a validar.
     */
    private void validarPeriodo(int mes, int anio) {
        if (mes < 1 || mes > 12) {
            throw new ReglaInvalidaException("El mes debe estar entre 1 y 12");
        }
        int anioMaximoAceptado = LocalDate.now(ZoneId.systemDefault()).getYear() + 1;
        if (anio < ANIO_MINIMO_ACEPTADO || anio > anioMaximoAceptado) {
            throw new ReglaInvalidaException("El anio debe estar entre " + ANIO_MINIMO_ACEPTADO
                    + " y " + anioMaximoAceptado);
        }
    }

    /**
     * Sincroniza el sueldo vigente del usuario (utilizado como respaldo en la division proporcional
     * de gastos) con el monto del periodo mas reciente que tenga registrado, o lo deja sin definir
     * si ya no cuenta con ningun sueldo registrado.
     *
     * @param usuario Usuario a resincronizar.
     */
    private void sincronizarSueldoVigente(Usuario usuario) {
        var montoVigente = sueldoRepository.findFirstByUsuario_IdOrderByAnioDescMesDesc(usuario.getId());
        usuario.setSueldo(montoVigente.map(s -> s.getMonto()).orElse(null));
        usuarioRepository.save(usuario);
    }
}
