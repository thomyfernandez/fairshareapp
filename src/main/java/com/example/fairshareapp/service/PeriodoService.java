package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.PeriodoCreateDTO;
import com.example.fairshareapp.model.dto.PeriodoResponseDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Periodo;
import com.example.fairshareapp.model.enums.EstadoPeriodo;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.LiquidacionRepository;
import com.example.fairshareapp.repository.PeriodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de negocio para la administracion de periodos mensuales de un espacio.
 * Un periodo, mientras esta ABIERTO, admite el registro de nuevos gastos fechados dentro de su
 * rango; al cerrarlo se bloquean nuevos gastos en ese rango y se dispara el calculo y la
 * persistencia de su Liquidacion a traves de LiquidacionService.
 */
@Service
@Transactional
public class PeriodoService {

    private final PeriodoRepository periodoRepository;
    private final EspacioRepository espacioRepository;
    private final LiquidacionRepository liquidacionRepository;
    private final LiquidacionService liquidacionService;

    /**
     * Constructor con inyeccion de dependencias de los repositorios y el servicio de liquidacion requeridos.
     *
     * @param periodoRepository Repositorio de persistencia de periodos.
     * @param espacioRepository Repositorio de persistencia de espacios.
     * @param liquidacionRepository Repositorio de persistencia de liquidaciones.
     * @param liquidacionService Servicio que calcula y persiste la liquidacion al cerrar un periodo.
     */
    public PeriodoService(PeriodoRepository periodoRepository,
                           EspacioRepository espacioRepository,
                           LiquidacionRepository liquidacionRepository,
                           LiquidacionService liquidacionService) {
        this.periodoRepository = periodoRepository;
        this.espacioRepository = espacioRepository;
        this.liquidacionRepository = liquidacionRepository;
        this.liquidacionService = liquidacionService;
    }

    /**
     * Abre un nuevo periodo mensual para un espacio. Falla si ya existe un periodo cargado
     * para ese anio y mes en el espacio.
     *
     * @param espacioId Identificador unico del espacio.
     * @param dto Datos del periodo a abrir.
     * @return Detalle del periodo recien creado.
     */
    public PeriodoResponseDTO abrirPeriodo(Long espacioId, PeriodoCreateDTO dto) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        if (periodoRepository.existsByEspacioIdAndAnioAndMes(espacioId, dto.getAnio(), dto.getMes())) {
            throw new ReglaInvalidaException("Ya existe un periodo cargado para " + dto.getMes() + "/" + dto.getAnio() +
                    " en el espacio con id " + espacioId);
        }

        Periodo periodo = Periodo.builder()
                .espacio(espacio)
                .anio(dto.getAnio())
                .mes(dto.getMes())
                .estado(EstadoPeriodo.ABIERTO)
                .build();

        return mapearADetalleDTO(periodoRepository.save(periodo));
    }

    /**
     * Lista los periodos de un espacio, ordenados del mas reciente al mas antiguo.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de periodos del espacio.
     */
    @Transactional(readOnly = true)
    public List<PeriodoResponseDTO> listarPeriodos(Long espacioId) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }
        return periodoRepository.findByEspacioIdOrderByAnioDescMesDesc(espacioId).stream()
                .map(this::mapearADetalleDTO)
                .toList();
    }

    /**
     * Obtiene el detalle de un periodo por su identificador.
     *
     * @param periodoId Identificador unico del periodo.
     * @return Detalle del periodo encontrado.
     */
    @Transactional(readOnly = true)
    public PeriodoResponseDTO obtenerPeriodoPorId(Long periodoId) {
        Periodo periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Periodo no encontrado con id: " + periodoId));
        return mapearADetalleDTO(periodo);
    }

    /**
     * Cierra un periodo abierto: lo marca CERRADO, fija su fecha de cierre y dispara la
     * generacion y persistencia de su liquidacion final.
     *
     * @param periodoId Identificador unico del periodo a cerrar.
     * @return Detalle de la liquidacion generada al cerrar el periodo.
     */
    public LiquidacionResponseDTO cerrarPeriodo(Long periodoId) {
        Periodo periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Periodo no encontrado con id: " + periodoId));

        if (periodo.getEstado() == EstadoPeriodo.CERRADO) {
            throw new ReglaInvalidaException("El periodo con id " + periodoId + " ya se encuentra cerrado");
        }

        periodo.setEstado(EstadoPeriodo.CERRADO);
        periodo.setFechaCierre(LocalDate.now());
        Periodo periodoCerrado = periodoRepository.save(periodo);

        return liquidacionService.generarYPersistir(periodoCerrado);
    }

    /**
     * Indica si existe, para un espacio, un periodo CERRADO cuyo rango de fechas cubra la fecha dada.
     * Utilizado por GastoService para bloquear el registro de nuevos gastos en periodos cerrados.
     *
     * @param espacioId Identificador unico del espacio.
     * @param fecha Fecha del gasto a validar.
     * @return true si la fecha cae dentro de un periodo cerrado del espacio.
     */
    @Transactional(readOnly = true)
    public boolean estaFechaEnPeriodoCerrado(Long espacioId, LocalDate fecha) {
        return periodoRepository
                .findByEspacioIdAndAnioAndMesAndEstado(espacioId, fecha.getYear(), fecha.getMonthValue(), EstadoPeriodo.CERRADO)
                .isPresent();
    }

    /**
     * Mapea una entidad Periodo a su correspondiente representacion DTO.
     *
     * @param periodo Entidad de periodo a transformar.
     * @return DTO con el detalle del periodo.
     */
    private PeriodoResponseDTO mapearADetalleDTO(Periodo periodo) {
        Long liquidacionId = liquidacionRepository.findByPeriodoId(periodo.getId())
                .map(liquidacion -> liquidacion.getId())
                .orElse(null);

        return PeriodoResponseDTO.builder()
                .id(periodo.getId())
                .espacioId(periodo.getEspacio().getId())
                .anio(periodo.getAnio())
                .mes(periodo.getMes())
                .estado(periodo.getEstado())
                .fechaInicio(periodo.getFechaInicio())
                .fechaFin(periodo.getFechaFin())
                .fechaCierre(periodo.getFechaCierre())
                .liquidacionId(liquidacionId)
                .build();
    }
}
