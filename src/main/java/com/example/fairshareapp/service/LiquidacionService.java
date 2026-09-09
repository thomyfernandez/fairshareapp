package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.LiquidacionTransferenciaDTO;
import com.example.fairshareapp.model.dto.ResumenUsuarioLiquidacionDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.GastoParticipante;
import com.example.fairshareapp.model.entity.Liquidacion;
import com.example.fairshareapp.model.entity.LiquidacionResumenUsuario;
import com.example.fairshareapp.model.entity.LiquidacionTransferencia;
import com.example.fairshareapp.model.entity.Periodo;
import com.example.fairshareapp.model.entity.Presupuesto;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.LiquidacionRepository;
import com.example.fairshareapp.repository.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de negocio para el calculo y consulta de liquidaciones de periodo.
 * Consolida los gastos de un periodo de un espacio para determinar las transferencias
 * minimas entre usuarios (reutilizando el algoritmo de SaldoCalculator) y evalua, para cada
 * usuario involucrado, si su gasto real supero el presupuesto mensual que tuviera cargado.
 * El "gasto real" de un usuario se calcula como la suma de sus cuotas como participante
 * (GastoParticipante.importe), no como lo que adelanto en su rol de pagador.
 */
@Service
@Transactional
public class LiquidacionService {

    private final LiquidacionRepository liquidacionRepository;
    private final GastoRepository gastoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final EspacioRepository espacioRepository;
    private final SaldoCalculator saldoCalculator;

    /**
     * Constructor con inyeccion de dependencias de los repositorios y el calculador de saldos requeridos.
     *
     * @param liquidacionRepository Repositorio de persistencia de liquidaciones.
     * @param gastoRepository Repositorio de persistencia de gastos.
     * @param presupuestoRepository Repositorio de persistencia de presupuestos.
     * @param espacioRepository Repositorio de persistencia de espacios.
     * @param saldoCalculator Componente que calcula saldos netos y los simplifica en transferencias minimas.
     */
    public LiquidacionService(LiquidacionRepository liquidacionRepository,
                               GastoRepository gastoRepository,
                               PresupuestoRepository presupuestoRepository,
                               EspacioRepository espacioRepository,
                               SaldoCalculator saldoCalculator) {
        this.liquidacionRepository = liquidacionRepository;
        this.gastoRepository = gastoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.espacioRepository = espacioRepository;
        this.saldoCalculator = saldoCalculator;
    }

    /**
     * Genera y persiste la liquidacion final de un periodo recien cerrado.
     * Solo debe invocarse una vez por periodo; PeriodoService la llama al momento del cierre.
     *
     * @param periodo Periodo recien marcado como cerrado.
     * @return Detalle de la liquidacion generada.
     */
    public LiquidacionResponseDTO generarYPersistir(Periodo periodo) {
        if (liquidacionRepository.existsByPeriodoId(periodo.getId())) {
            throw new ReglaInvalidaException("Ya existe una liquidacion generada para el periodo con id " + periodo.getId());
        }

        Espacio espacio = periodo.getEspacio();
        List<Gasto> gastos = gastoRepository.findByEspacioIdAndFechaBetweenOrderByFechaDesc(
                espacio.getId(), periodo.getFechaInicio(), periodo.getFechaFin());

        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        Map<Long, BigDecimal> saldosNetos = saldoCalculator.calcularSaldosNetos(gastos, usuariosPorId);
        List<TransferenciaCalculada> transferencias = saldoCalculator.simplificarTransacciones(saldosNetos);

        BigDecimal montoTotalGastos = gastos.stream()
                .map(Gasto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> gastadoPorUsuario = calcularGastadoPorUsuario(gastos, usuariosPorId);

        Liquidacion liquidacion = Liquidacion.builder()
                .periodo(periodo)
                .espacio(espacio)
                .fechaGeneracion(LocalDate.now())
                .montoTotalGastos(montoTotalGastos)
                .build();

        for (TransferenciaCalculada transferencia : transferencias) {
            liquidacion.agregarTransferencia(LiquidacionTransferencia.builder()
                    .deudor(usuariosPorId.get(transferencia.deudorId()))
                    .acreedor(usuariosPorId.get(transferencia.acreedorId()))
                    .monto(transferencia.monto())
                    .build());
        }

        for (Map.Entry<Long, BigDecimal> entry : gastadoPorUsuario.entrySet()) {
            Usuario usuario = usuariosPorId.get(entry.getKey());
            BigDecimal totalGastado = entry.getValue();

            Optional<Presupuesto> presupuesto = presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(
                    espacio.getId(), usuario.getId(), periodo.getAnio(), periodo.getMes());

            liquidacion.agregarResumenUsuario(construirResumenUsuario(usuario, totalGastado, presupuesto.orElse(null)));
        }

        Liquidacion guardada = liquidacionRepository.save(liquidacion);
        return mapearADetalleDTO(guardada);
    }

    /**
     * Calcula una vista previa no persistida de la liquidacion de un anio y mes de un espacio,
     * util para consultar el estado de un periodo aun abierto o inexistente antes de cerrarlo.
     *
     * @param espacioId Identificador unico del espacio.
     * @param anio Anio a previsualizar.
     * @param mes Mes a previsualizar.
     * @return Detalle de la liquidacion calculada, con id nulo por no estar persistida.
     */
    @Transactional(readOnly = true)
    public LiquidacionResponseDTO previsualizar(Long espacioId, Integer anio, Integer mes) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        YearMonth yearMonth = YearMonth.of(anio, mes);
        List<Gasto> gastos = gastoRepository.findByEspacioIdAndFechaBetweenOrderByFechaDesc(
                espacioId, yearMonth.atDay(1), yearMonth.atEndOfMonth());

        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        Map<Long, BigDecimal> saldosNetos = saldoCalculator.calcularSaldosNetos(gastos, usuariosPorId);
        List<TransferenciaCalculada> transferencias = saldoCalculator.simplificarTransacciones(saldosNetos);

        BigDecimal montoTotalGastos = gastos.stream()
                .map(Gasto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> gastadoPorUsuario = calcularGastadoPorUsuario(gastos, usuariosPorId);

        List<LiquidacionTransferenciaDTO> transferenciasDTO = transferencias.stream()
                .map(t -> mapearATransferenciaDTO(usuariosPorId.get(t.deudorId()), usuariosPorId.get(t.acreedorId()), t.monto()))
                .toList();

        List<ResumenUsuarioLiquidacionDTO> resumenDTO = gastadoPorUsuario.entrySet().stream()
                .map(entry -> {
                    Usuario usuario = usuariosPorId.get(entry.getKey());
                    Optional<Presupuesto> presupuesto = presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(
                            espacioId, usuario.getId(), anio, mes);
                    return mapearAResumenDTO(usuario, entry.getValue(), presupuesto.orElse(null));
                })
                .toList();

        return LiquidacionResponseDTO.builder()
                .id(null)
                .periodoId(null)
                .espacioId(espacio.getId())
                .anio(anio)
                .mes(mes)
                .fechaGeneracion(LocalDate.now())
                .montoTotalGastos(montoTotalGastos)
                .transferencias(transferenciasDTO)
                .resumenPorUsuario(resumenDTO)
                .build();
    }

    /**
     * Lista las liquidaciones historicas generadas para un espacio, de la mas reciente a la mas antigua.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Lista de liquidaciones del espacio.
     */
    @Transactional(readOnly = true)
    public List<LiquidacionResponseDTO> listarPorEspacio(Long espacioId) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }
        return liquidacionRepository.findByEspacioIdOrderByFechaGeneracionDesc(espacioId).stream()
                .map(this::mapearADetalleDTO)
                .toList();
    }

    /**
     * Obtiene el detalle completo de una liquidacion por su identificador.
     *
     * @param liquidacionId Identificador unico de la liquidacion.
     * @return Detalle de la liquidacion encontrada.
     */
    @Transactional(readOnly = true)
    public LiquidacionResponseDTO obtenerPorId(Long liquidacionId) {
        Liquidacion liquidacion = liquidacionRepository.findById(liquidacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Liquidacion no encontrada con id: " + liquidacionId));
        return mapearADetalleDTO(liquidacion);
    }

    /**
     * Calcula, para cada usuario involucrado en una lista de gastos, la suma de sus cuotas
     * como participante: representa lo que efectivamente le costo el periodo, a diferencia
     * de lo que haya adelantado en su rol de pagador.
     *
     * @param gastos Lista de gastos del periodo.
     * @param usuariosPorId Mapa de usuarios involucrados, completado por SaldoCalculator.
     * @return Mapa de identificador de usuario a el total gastado como participante.
     */
    private Map<Long, BigDecimal> calcularGastadoPorUsuario(List<Gasto> gastos, Map<Long, Usuario> usuariosPorId) {
        Map<Long, BigDecimal> gastadoPorUsuario = new HashMap<>();
        for (Gasto gasto : gastos) {
            for (GastoParticipante participante : gasto.getParticipantes()) {
                Usuario usuario = participante.getUsuario();
                usuariosPorId.put(usuario.getId(), usuario);
                gastadoPorUsuario.merge(usuario.getId(), participante.getImporte(), BigDecimal::add);
            }
        }
        return gastadoPorUsuario;
    }

    /**
     * Construye el resumen de gasto y presupuesto de un usuario, evaluando si corresponde
     * marcar excedente segun el presupuesto que tuviera cargado para el periodo.
     *
     * @param usuario Usuario al que corresponde el resumen.
     * @param totalGastado Total gastado por el usuario como participante en el periodo.
     * @param presupuesto Presupuesto cargado por el usuario para el periodo, o null si no tiene.
     * @return Entidad LiquidacionResumenUsuario lista para asociar a la liquidacion.
     */
    private LiquidacionResumenUsuario construirResumenUsuario(Usuario usuario, BigDecimal totalGastado, Presupuesto presupuesto) {
        Boolean excedido = null;
        BigDecimal montoExcedente = null;
        BigDecimal presupuestoLimite = null;

        if (presupuesto != null) {
            presupuestoLimite = presupuesto.getMontoLimite();
            excedido = totalGastado.compareTo(presupuestoLimite) > 0;
            montoExcedente = excedido ? totalGastado.subtract(presupuestoLimite) : BigDecimal.ZERO;
        }

        return LiquidacionResumenUsuario.builder()
                .usuario(usuario)
                .totalGastado(totalGastado)
                .presupuestoLimite(presupuestoLimite)
                .presupuestoExcedido(excedido)
                .montoExcedente(montoExcedente)
                .build();
    }

    /**
     * Mapea una entidad Liquidacion a su correspondiente representacion DTO.
     *
     * @param liquidacion Entidad de liquidacion a transformar.
     * @return DTO con el detalle completo de la liquidacion.
     */
    private LiquidacionResponseDTO mapearADetalleDTO(Liquidacion liquidacion) {
        List<LiquidacionTransferenciaDTO> transferenciasDTO = liquidacion.getTransferencias().stream()
                .map(t -> mapearATransferenciaDTO(t.getDeudor(), t.getAcreedor(), t.getMonto()))
                .toList();

        List<ResumenUsuarioLiquidacionDTO> resumenDTO = liquidacion.getResumenPorUsuario().stream()
                .map(r -> ResumenUsuarioLiquidacionDTO.builder()
                        .usuarioId(r.getUsuario().getId())
                        .usuarioNombre(r.getUsuario().getNombre() + " " + r.getUsuario().getApellido())
                        .totalGastado(r.getTotalGastado())
                        .presupuestoLimite(r.getPresupuestoLimite())
                        .presupuestoExcedido(r.getPresupuestoExcedido())
                        .montoExcedente(r.getMontoExcedente())
                        .build())
                .toList();

        return LiquidacionResponseDTO.builder()
                .id(liquidacion.getId())
                .periodoId(liquidacion.getPeriodo().getId())
                .espacioId(liquidacion.getEspacio().getId())
                .anio(liquidacion.getPeriodo().getAnio())
                .mes(liquidacion.getPeriodo().getMes())
                .fechaGeneracion(liquidacion.getFechaGeneracion())
                .montoTotalGastos(liquidacion.getMontoTotalGastos())
                .transferencias(transferenciasDTO)
                .resumenPorUsuario(resumenDTO)
                .build();
    }

    /**
     * Mapea un par deudor-acreedor y su monto a su correspondiente representacion DTO.
     *
     * @param deudor Usuario que debe transferir el dinero.
     * @param acreedor Usuario que debe recibir el dinero.
     * @param monto Monto a transferir.
     * @return DTO con el detalle de la transferencia.
     */
    private LiquidacionTransferenciaDTO mapearATransferenciaDTO(Usuario deudor, Usuario acreedor, BigDecimal monto) {
        return LiquidacionTransferenciaDTO.builder()
                .deudorId(deudor.getId())
                .deudorNombre(deudor.getNombre() + " " + deudor.getApellido())
                .acreedorId(acreedor.getId())
                .acreedorNombre(acreedor.getNombre() + " " + acreedor.getApellido())
                .monto(monto)
                .build();
    }

    /**
     * Mapea el resumen de gasto y presupuesto de un usuario (aun no persistido) a su DTO,
     * utilizado por la previsualizacion de un periodo todavia abierto.
     *
     * @param usuario Usuario al que corresponde el resumen.
     * @param totalGastado Total gastado por el usuario como participante en el periodo.
     * @param presupuesto Presupuesto cargado por el usuario para el periodo, o null si no tiene.
     * @return DTO con el resumen de gasto y presupuesto del usuario.
     */
    private ResumenUsuarioLiquidacionDTO mapearAResumenDTO(Usuario usuario, BigDecimal totalGastado, Presupuesto presupuesto) {
        LiquidacionResumenUsuario resumen = construirResumenUsuario(usuario, totalGastado, presupuesto);
        return ResumenUsuarioLiquidacionDTO.builder()
                .usuarioId(usuario.getId())
                .usuarioNombre(usuario.getNombre() + " " + usuario.getApellido())
                .totalGastado(resumen.getTotalGastado())
                .presupuestoLimite(resumen.getPresupuestoLimite())
                .presupuestoExcedido(resumen.getPresupuestoExcedido())
                .montoExcedente(resumen.getMontoExcedente())
                .build();
    }
}
