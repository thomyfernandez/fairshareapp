package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.BalanceDTO;
import com.example.fairshareapp.model.dto.DeudaDetalleDTO;
import com.example.fairshareapp.model.dto.RegistrarPagoDTO;
import com.example.fairshareapp.model.enums.EstadoDeuda;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.SaldoDeuda;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.SaldoDeudaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de negocio para el motor de balances, deudas y liquidacion cruzada.
 * Consolida los gastos registrados en un espacio para determinar cuanto le debe
 * cada usuario a cada otro, simplifica esa informacion en la minima cantidad de
 * transacciones posible y permite saldar deudas individualmente.
 */
@Service
@Transactional
public class BalanceService {

    private static final BigDecimal TOLERANCIA = BigDecimal.valueOf(0.01);

    private final SaldoDeudaRepository saldoDeudaRepository;
    private final GastoRepository gastoRepository;
    private final EspacioRepository espacioRepository;
    private final SaldoCalculator saldoCalculator;

    /**
     * Constructor con inyeccion de dependencias de los repositorios y el calculador de saldos requeridos.
     *
     * @param saldoDeudaRepository Repositorio de persistencia de deudas.
     * @param gastoRepository Repositorio de persistencia de gastos.
     * @param espacioRepository Repositorio de persistencia de espacios.
     * @param saldoCalculator Componente que calcula saldos netos y los simplifica en transferencias minimas.
     */
    public BalanceService(SaldoDeudaRepository saldoDeudaRepository,
                          GastoRepository gastoRepository,
                          EspacioRepository espacioRepository,
                          SaldoCalculator saldoCalculator) {
        this.saldoDeudaRepository = saldoDeudaRepository;
        this.gastoRepository = gastoRepository;
        this.espacioRepository = espacioRepository;
        this.saldoCalculator = saldoCalculator;
    }

    /**
     * Calcula la matriz simplificada de deudas de un espacio a partir de sus gastos,
     * sincroniza el resultado con las deudas persistidas y devuelve el balance vigente.
     *
     * @param espacioId Identificador unico del espacio.
     * @return Balance con la matriz simplificada de deudas pendientes.
     */
    public BalanceDTO obtenerBalance(Long espacioId) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        List<Gasto> gastos = gastoRepository.findByEspacioIdOrderByFechaDesc(espacioId);

        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        Map<Long, BigDecimal> saldosNetos = saldoCalculator.calcularSaldosNetos(gastos, usuariosPorId);
        List<TransferenciaCalculada> transferencias = saldoCalculator.simplificarTransacciones(saldosNetos);

        List<SaldoDeuda> deudasVigentes = sincronizarSaldoDeuda(espacio, transferencias, usuariosPorId);

        List<DeudaDetalleDTO> deudasDTO = deudasVigentes.stream()
                .map(this::mapearADetalleDTO)
                .toList();

        return BalanceDTO.builder()
                .espacioId(espacio.getId())
                .espacioNombre(espacio.getNombre())
                .deudas(deudasDTO)
                .build();
    }

    /**
     * Registra el pago de una deuda, total o parcial, y actualiza su estado.
     *
     * @param saldoDeudaId Identificador de la deuda a saldar.
     * @param dto Datos del pago a registrar. Si no incluye monto, se interpreta como pago total.
     * @return Detalle actualizado de la deuda.
     */
    public DeudaDetalleDTO registrarPago(Long saldoDeudaId, RegistrarPagoDTO dto) {
        SaldoDeuda deuda = saldoDeudaRepository.findById(saldoDeudaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Deuda no encontrada con id: " + saldoDeudaId));

        if (deuda.getEstado() == EstadoDeuda.SALDADO) {
            throw new ReglaInvalidaException("La deuda con id " + saldoDeudaId + " ya se encuentra saldada");
        }

        BigDecimal montoPendiente = deuda.getMonto();
        BigDecimal montoPagado = (dto != null && dto.getMonto() != null)
                ? dto.getMonto()
                : montoPendiente;

        if (montoPagado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaInvalidaException("El monto del pago debe ser un valor positivo");
        }
        if (montoPagado.subtract(montoPendiente).compareTo(TOLERANCIA) > 0) {
            throw new ReglaInvalidaException("El monto del pago (" + montoPagado +
                    ") supera el saldo pendiente de la deuda (" + montoPendiente + ")");
        }

        BigDecimal nuevoPendiente = montoPendiente.subtract(montoPagado).setScale(2, RoundingMode.HALF_UP);
        if (nuevoPendiente.compareTo(TOLERANCIA) <= 0) {
            deuda.setMonto(BigDecimal.ZERO);
            deuda.setEstado(EstadoDeuda.SALDADO);
        } else {
            deuda.setMonto(nuevoPendiente);
        }

        if (dto != null && dto.getLiquidacionId() != null) {
            deuda.setLiquidacionId(dto.getLiquidacionId());
        }

        SaldoDeuda deudaActualizada = saldoDeudaRepository.save(deuda);
        return mapearADetalleDTO(deudaActualizada);
    }

    /**
     * Sincroniza las transferencias simplificadas con las deudas persistidas del espacio.
     * Actualiza incrementalmente las deudas pendientes existentes (respetando lo ya pagado),
     * crea las nuevas relaciones de deuda detectadas y salda las que dejaron de tener vigencia.
     *
     * @param espacio Espacio al que pertenecen las deudas.
     * @param transferencias Transferencias simplificadas calculadas en este recalculo.
     * @param usuariosPorId Mapa de usuarios involucrados, para asociar las entidades sin consultas adicionales.
     * @return Lista de deudas pendientes vigentes luego de la sincronizacion.
     */
    private List<SaldoDeuda> sincronizarSaldoDeuda(Espacio espacio, List<TransferenciaCalculada> transferencias, Map<Long, Usuario> usuariosPorId) {
        List<SaldoDeuda> pendientesExistentes = saldoDeudaRepository.findByEspacioIdAndEstado(espacio.getId(), EstadoDeuda.PENDIENTE);
        Map<String, SaldoDeuda> pendientesPorPar = new HashMap<>();
        for (SaldoDeuda pendiente : pendientesExistentes) {
            pendientesPorPar.put(clavePar(pendiente.getDeudor().getId(), pendiente.getAcreedor().getId()), pendiente);
        }

        List<SaldoDeuda> vigentes = new ArrayList<>();

        for (TransferenciaCalculada transferencia : transferencias) {
            String clave = clavePar(transferencia.deudorId(), transferencia.acreedorId());
            SaldoDeuda existente = pendientesPorPar.remove(clave);

            if (existente != null) {
                BigDecimal delta = transferencia.monto().subtract(existente.getMontoOriginal());
                BigDecimal nuevoPendiente = existente.getMonto().add(delta).max(BigDecimal.ZERO);

                existente.setMontoOriginal(transferencia.monto());
                if (nuevoPendiente.compareTo(TOLERANCIA) <= 0) {
                    existente.setMonto(BigDecimal.ZERO);
                    existente.setEstado(EstadoDeuda.SALDADO);
                } else {
                    existente.setMonto(nuevoPendiente.setScale(2, RoundingMode.HALF_UP));
                }
                vigentes.add(saldoDeudaRepository.save(existente));
            } else {
                SaldoDeuda nueva = SaldoDeuda.builder()
                        .espacio(espacio)
                        .deudor(usuariosPorId.get(transferencia.deudorId()))
                        .acreedor(usuariosPorId.get(transferencia.acreedorId()))
                        .montoOriginal(transferencia.monto())
                        .monto(transferencia.monto())
                        .estado(EstadoDeuda.PENDIENTE)
                        .build();
                vigentes.add(saldoDeudaRepository.save(nueva));
            }
        }

        // Las deudas que quedaron sin transferencia simplificada equivalente ya no tienen vigencia
        for (SaldoDeuda obsoleta : pendientesPorPar.values()) {
            obsoleta.setMonto(BigDecimal.ZERO);
            obsoleta.setEstado(EstadoDeuda.SALDADO);
            saldoDeudaRepository.save(obsoleta);
        }

        return vigentes.stream().filter(d -> d.getEstado() == EstadoDeuda.PENDIENTE).toList();
    }

    /**
     * Construye una clave unica para identificar el par ordenado deudor-acreedor.
     *
     * @param deudorId Identificador del deudor.
     * @param acreedorId Identificador del acreedor.
     * @return Clave de texto que identifica el par.
     */
    private String clavePar(Long deudorId, Long acreedorId) {
        return deudorId + ":" + acreedorId;
    }

    /**
     * Mapea una entidad SaldoDeuda a su correspondiente representacion DTO.
     *
     * @param deuda Entidad de deuda a transformar.
     * @return DTO con el detalle de la deuda.
     */
    private DeudaDetalleDTO mapearADetalleDTO(SaldoDeuda deuda) {
        Usuario deudor = deuda.getDeudor();
        Usuario acreedor = deuda.getAcreedor();

        return DeudaDetalleDTO.builder()
                .id(deuda.getId())
                .deudorId(deudor.getId())
                .deudorNombre(deudor.getNombre() + " " + deudor.getApellido())
                .acreedorId(acreedor.getId())
                .acreedorNombre(acreedor.getNombre() + " " + acreedor.getApellido())
                .monto(deuda.getMonto())
                .estado(deuda.getEstado())
                .liquidacionId(deuda.getLiquidacionId())
                .build();
    }
}
