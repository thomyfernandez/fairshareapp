package com.example.fairshareapp.service.impl;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.ProcesarLiquidacionDTO;
import com.example.fairshareapp.model.dto.ResumenCierreDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.Liquidacion;
import com.example.fairshareapp.model.enums.EstadoGasto;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.LiquidacionRepository;
import com.example.fairshareapp.service.LiquidacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion del servicio de liquidaciones de espacios compartidos.
 * Realiza el proceso de checkout validando presupuesto disponible, efectuando el debito
 * y cambiando el estado de los gastos a LIQUIDADO.
 */
@Service
@Transactional
public class LiquidacionServiceImpl implements LiquidacionService {

    private final LiquidacionRepository liquidacionRepository;
    private final EspacioRepository espacioRepository;
    private final GastoRepository gastoRepository;

    /**
     * Constructor para la inyeccion de dependencias de repositorios.
     *
     * @param liquidacionRepository Repositorio de liquidaciones.
     * @param espacioRepository Repositorio de espacios.
     * @param gastoRepository Repositorio de gastos.
     */
    public LiquidacionServiceImpl(LiquidacionRepository liquidacionRepository,
                                  EspacioRepository espacioRepository,
                                  GastoRepository gastoRepository) {
        this.liquidacionRepository = liquidacionRepository;
        this.espacioRepository = espacioRepository;
        this.gastoRepository = gastoRepository;
    }

    /**
     * Procesa el corte y liquidacion de gastos verificando fondos disponibles en el espacio.
     *
     * @param espacioId Identificador unico del espacio.
     * @param dto Parametros opcionales de cierre.
     * @return LiquidacionResponseDTO con el detalle del cierre.
     */
    @Override
    public LiquidacionResponseDTO procesarCierreLiquidacion(Long espacioId, ProcesarLiquidacionDTO dto) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        List<Gasto> gastosALiquidar;
        if (dto != null && dto.getGastoIds() != null && !dto.getGastoIds().isEmpty()) {
            gastosALiquidar = gastoRepository.findByIdInAndEspacioId(dto.getGastoIds(), espacioId).stream()
                    .filter(g -> g.getEstado() == EstadoGasto.PENDIENTE)
                    .toList();
        } else {
            gastosALiquidar = gastoRepository.findByEspacioIdAndEstadoOrderByFechaAsc(espacioId, EstadoGasto.PENDIENTE);
        }

        if (gastosALiquidar.isEmpty()) {
            throw new ReglaInvalidaException("No existen gastos pendientes para liquidar en el espacio");
        }

        // Calcula el monto acumulado del lote a cerrar
        BigDecimal totalLiquidado = BigDecimal.ZERO;
        List<Long> gastosLiquidadosIds = new ArrayList<>();
        for (Gasto gasto : gastosALiquidar) {
            totalLiquidado = totalLiquidado.add(gasto.getMonto());
            gastosLiquidadosIds.add(gasto.getId());
        }

        // Control de stock / presupuesto disponible en el espacio
        BigDecimal presupuestoAnterior = espacio.getPresupuestoBase() != null ? espacio.getPresupuestoBase() : BigDecimal.ZERO;
        if (presupuestoAnterior.compareTo(totalLiquidado) < 0) {
            throw new ReglaInvalidaException("El total a liquidar (" + totalLiquidado +
                    ") supera el presupuesto disponible del espacio (" + presupuestoAnterior + ")");
        }

        // Descuenta el total del presupuesto base del espacio
        BigDecimal presupuestoRestante = presupuestoAnterior.subtract(totalLiquidado);
        espacio.setPresupuestoBase(presupuestoRestante);
        espacioRepository.save(espacio);

        LocalDate fechaCierre = LocalDate.now(ZoneId.systemDefault());
        int mes = (dto != null && dto.getMes() != null) ? dto.getMes() : fechaCierre.getMonthValue();
        int anio = (dto != null && dto.getAnio() != null) ? dto.getAnio() : fechaCierre.getYear();
        String descripcion = dto != null ? dto.getDescripcion() : null;

        Liquidacion liquidacion = Liquidacion.builder()
                .espacio(espacio)
                .montoTotal(totalLiquidado)
                .fechaLiquidacion(fechaCierre)
                .mes(mes)
                .anio(anio)
                .descripcion(descripcion)
                .build();
        liquidacion = liquidacionRepository.save(liquidacion);

        // Transiciona los gastos a estado LIQUIDADO y los vincula a la liquidacion
        for (Gasto gasto : gastosALiquidar) {
            gasto.setEstado(EstadoGasto.LIQUIDADO);
            gasto.setLiquidacion(liquidacion);
        }
        gastoRepository.saveAll(gastosALiquidar);

        ResumenCierreDTO resumenCierre = ResumenCierreDTO.builder()
                .totalLiquidado(totalLiquidado)
                .presupuestoAnterior(presupuestoAnterior)
                .presupuestoRestante(presupuestoRestante)
                .cantidadGastosCerrados(gastosALiquidar.size())
                .fechaCierre(fechaCierre)
                .build();

        return LiquidacionResponseDTO.builder()
                .id(liquidacion.getId())
                .espacioId(espacio.getId())
                .espacioNombre(espacio.getNombre())
                .montoTotal(totalLiquidado)
                .fechaLiquidacion(fechaCierre)
                .mes(mes)
                .anio(anio)
                .descripcion(descripcion)
                .cantidadGastos(gastosALiquidar.size())
                .gastosLiquidadosIds(gastosLiquidadosIds)
                .resumenCierre(resumenCierre)
                .build();
    }

    /**
     * Consulta el historial de liquidaciones cerradas para un espacio con opciones de filtrado.
     *
     * @param espacioId Identificador unico del espacio.
     * @param desde Fecha de inicio opcional.
     * @param hasta Fecha de fin opcional.
     * @param anio Anio del periodo opcional.
     * @param mes Mes del periodo opcional.
     * @return Lista de LiquidacionResponseDTO encontradas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionResponseDTO> obtenerHistorial(Long espacioId, LocalDate desde, LocalDate hasta, Integer anio, Integer mes) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }

        List<Liquidacion> liquidaciones;
        if (desde != null && hasta != null) {
            liquidaciones = liquidacionRepository.findByEspacio_IdAndFechaLiquidacionBetweenOrderByFechaLiquidacionDesc(espacioId, desde, hasta);
        } else if (anio != null && mes != null) {
            liquidaciones = liquidacionRepository.findByEspacio_IdAndAnioAndMesOrderByFechaLiquidacionDesc(espacioId, anio, mes);
        } else {
            liquidaciones = liquidacionRepository.findByEspacio_IdOrderByFechaLiquidacionDesc(espacioId);
        }

        List<LiquidacionResponseDTO> resultado = new ArrayList<>();
        for (Liquidacion l : liquidaciones) {
            resultado.add(mapearAResponseDTO(l));
        }
        return resultado;
    }

    /**
     * Mapea una entidad Liquidacion a su correspondiente DTO de respuesta.
     *
     * @param l Entidad Liquidacion a transformar.
     * @return DTO de respuesta con la informacion de la liquidacion.
     */
    private LiquidacionResponseDTO mapearAResponseDTO(Liquidacion l) {
        List<Long> ids = new ArrayList<>();
        if (l.getGastos() != null) {
            for (Gasto g : l.getGastos()) {
                ids.add(g.getId());
            }
        }
        return LiquidacionResponseDTO.builder()
                .id(l.getId())
                .espacioId(l.getEspacio().getId())
                .espacioNombre(l.getEspacio().getNombre())
                .montoTotal(l.getMontoTotal())
                .fechaLiquidacion(l.getFechaLiquidacion())
                .mes(l.getMes())
                .anio(l.getAnio())
                .descripcion(l.getDescripcion())
                .cantidadGastos(ids.size())
                .gastosLiquidadosIds(ids)
                .build();
    }
}
