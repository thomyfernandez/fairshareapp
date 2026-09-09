package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.BalanceDTO;
import com.example.fairshareapp.model.dto.DeudaDetalleDTO;
import com.example.fairshareapp.model.dto.RegistrarPagoDTO;
import com.example.fairshareapp.model.entity.EstadoDeuda;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.GastoParticipante;
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

    /**
     * Constructor con inyeccion de dependencias de los repositorios requeridos.
     *
     * @param saldoDeudaRepository Repositorio de persistencia de deudas.
     * @param gastoRepository Repositorio de persistencia de gastos.
     * @param espacioRepository Repositorio de persistencia de espacios.
     */
    public BalanceService(SaldoDeudaRepository saldoDeudaRepository,
                          GastoRepository gastoRepository,
                          EspacioRepository espacioRepository) {
        this.saldoDeudaRepository = saldoDeudaRepository;
        this.gastoRepository = gastoRepository;
        this.espacioRepository = espacioRepository;
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
        Map<Long, BigDecimal> saldosNetos = calcularSaldosNetos(gastos, usuariosPorId);
        List<Transaccion> transacciones = simplificarTransacciones(saldosNetos);

        List<SaldoDeuda> deudasVigentes = sincronizarSaldoDeuda(espacio, transacciones, usuariosPorId);

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

        BigDecimal montoPendiente = BigDecimal.valueOf(deuda.getMonto());
        BigDecimal montoPagado = (dto != null && dto.getMonto() != null)
                ? BigDecimal.valueOf(dto.getMonto())
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
            deuda.setMonto(0.0);
            deuda.setEstado(EstadoDeuda.SALDADO);
        } else {
            deuda.setMonto(nuevoPendiente.doubleValue());
        }

        if (dto != null && dto.getLiquidacionId() != null) {
            deuda.setLiquidacionId(dto.getLiquidacionId());
        }

        SaldoDeuda deudaActualizada = saldoDeudaRepository.save(deuda);
        return mapearADetalleDTO(deudaActualizada);
    }

    /**
     * Calcula el saldo neto de cada usuario dentro de un espacio: positivo si le deben plata,
     * negativo si el debe. Recorre cada gasto sumando al pagador lo que adelanto y restando
     * a cada participante la cuota que le correspondia cubrir.
     *
     * @param gastos Lista de gastos del espacio.
     * @param usuariosPorId Mapa de salida que se completa con los usuarios involucrados, para evitar consultas adicionales.
     * @return Mapa de identificador de usuario a su saldo neto.
     */
    @SuppressWarnings("null")
    private Map<Long, BigDecimal> calcularSaldosNetos(List<Gasto> gastos, Map<Long, Usuario> usuariosPorId) {
        Map<Long, BigDecimal> saldosNetos = new HashMap<>();

        for (Gasto gasto : gastos) {
            Usuario pagador = gasto.getPagador();
            usuariosPorId.put(pagador.getId(), pagador);

            for (GastoParticipante participante : gasto.getParticipantes()) {
                Usuario usuario = participante.getUsuario();
                usuariosPorId.put(usuario.getId(), usuario);

                BigDecimal importe = BigDecimal.valueOf(participante.getImporte());
                saldosNetos.merge(usuario.getId(), importe.negate(), BigDecimal::add);
                saldosNetos.merge(pagador.getId(), importe, BigDecimal::add);
            }
        }

        return saldosNetos;
    }

    /**
     * Simplifica los saldos netos en la minima cantidad de transacciones posible,
     * emparejando en cada paso al deudor y al acreedor con mayor magnitud pendiente.
     *
     * @param saldosNetos Mapa de identificador de usuario a su saldo neto.
     * @return Lista de transacciones simplificadas (quien le debe a quien y cuanto).
     */
    private List<Transaccion> simplificarTransacciones(Map<Long, BigDecimal> saldosNetos) {
        List<SaldoPersona> deudores = new ArrayList<>();
        List<SaldoPersona> acreedores = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : saldosNetos.entrySet()) {
            BigDecimal saldo = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            if (saldo.compareTo(TOLERANCIA.negate()) < 0) {
                deudores.add(new SaldoPersona(entry.getKey(), saldo.negate()));
            } else if (saldo.compareTo(TOLERANCIA) > 0) {
                acreedores.add(new SaldoPersona(entry.getKey(), saldo));
            }
        }

        deudores.sort((a, b) -> b.monto.compareTo(a.monto));
        acreedores.sort((a, b) -> b.monto.compareTo(a.monto));

        List<Transaccion> transacciones = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < deudores.size() && j < acreedores.size()) {
            SaldoPersona deudor = deudores.get(i);
            SaldoPersona acreedor = acreedores.get(j);
            BigDecimal monto = deudor.monto.min(acreedor.monto);

            if (monto.compareTo(TOLERANCIA) > 0) {
                transacciones.add(new Transaccion(deudor.usuarioId, acreedor.usuarioId, monto));
            }

            deudor.monto = deudor.monto.subtract(monto);
            acreedor.monto = acreedor.monto.subtract(monto);

            if (deudor.monto.compareTo(TOLERANCIA) <= 0) {
                i++;
            }
            if (acreedor.monto.compareTo(TOLERANCIA) <= 0) {
                j++;
            }
        }

        return transacciones;
    }

    /**
     * Sincroniza las transacciones simplificadas con las deudas persistidas del espacio.
     * Actualiza incrementalmente las deudas pendientes existentes (respetando lo ya pagado),
     * crea las nuevas relaciones de deuda detectadas y salda las que dejaron de tener vigencia.
     *
     * @param espacio Espacio al que pertenecen las deudas.
     * @param transacciones Transacciones simplificadas calculadas en este recalculo.
     * @param usuariosPorId Mapa de usuarios involucrados, para asociar las entidades sin consultas adicionales.
     * @return Lista de deudas pendientes vigentes luego de la sincronizacion.
     */
    private List<SaldoDeuda> sincronizarSaldoDeuda(Espacio espacio, List<Transaccion> transacciones, Map<Long, Usuario> usuariosPorId) {
        List<SaldoDeuda> pendientesExistentes = saldoDeudaRepository.findByEspacioIdAndEstado(espacio.getId(), EstadoDeuda.PENDIENTE);
        Map<String, SaldoDeuda> pendientesPorPar = new HashMap<>();
        for (SaldoDeuda pendiente : pendientesExistentes) {
            pendientesPorPar.put(clavePar(pendiente.getDeudor().getId(), pendiente.getAcreedor().getId()), pendiente);
        }

        List<SaldoDeuda> vigentes = new ArrayList<>();

        for (Transaccion transaccion : transacciones) {
            String clave = clavePar(transaccion.deudorId, transaccion.acreedorId);
            SaldoDeuda existente = pendientesPorPar.remove(clave);

            if (existente != null) {
                BigDecimal delta = transaccion.monto.subtract(BigDecimal.valueOf(existente.getMontoOriginal()));
                BigDecimal nuevoPendiente = BigDecimal.valueOf(existente.getMonto()).add(delta).max(BigDecimal.ZERO);

                existente.setMontoOriginal(transaccion.monto.doubleValue());
                if (nuevoPendiente.compareTo(TOLERANCIA) <= 0) {
                    existente.setMonto(0.0);
                    existente.setEstado(EstadoDeuda.SALDADO);
                } else {
                    existente.setMonto(nuevoPendiente.setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                vigentes.add(saldoDeudaRepository.save(existente));
            } else {
                SaldoDeuda nueva = SaldoDeuda.builder()
                        .espacio(espacio)
                        .deudor(usuariosPorId.get(transaccion.deudorId))
                        .acreedor(usuariosPorId.get(transaccion.acreedorId))
                        .montoOriginal(transaccion.monto.doubleValue())
                        .monto(transaccion.monto.doubleValue())
                        .estado(EstadoDeuda.PENDIENTE)
                        .build();
                vigentes.add(saldoDeudaRepository.save(nueva));
            }
        }

        // Las deudas que quedaron sin transaccion simplificada equivalente ya no tienen vigencia
        for (SaldoDeuda obsoleta : pendientesPorPar.values()) {
            obsoleta.setMonto(0.0);
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

    /**
     * Representa una transaccion simplificada resultante del algoritmo de consolidacion:
     * el deudor debe transferir el monto indicado al acreedor.
     */
    private record Transaccion(Long deudorId, Long acreedorId, BigDecimal monto) {
    }

    /**
     * Estructura mutable auxiliar para el algoritmo greedy de simplificacion,
     * que necesita ir reduciendo el monto pendiente de cada persona en cada iteracion.
     */
    private static final class SaldoPersona {
        private final Long usuarioId;
        private BigDecimal monto;

        private SaldoPersona(Long usuarioId, BigDecimal monto) {
            this.usuarioId = usuarioId;
            this.monto = monto;
        }
    }
}
