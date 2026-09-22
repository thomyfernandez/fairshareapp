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

@Service
@Transactional
public class LiquidacionServiceImpl implements LiquidacionService {

    private final LiquidacionRepository liquidacionRepository;
    private final EspacioRepository espacioRepository;
    private final GastoRepository gastoRepository;

    public LiquidacionServiceImpl(LiquidacionRepository liquidacionRepository,
                                  EspacioRepository espacioRepository,
                                  GastoRepository gastoRepository) {
        this.liquidacionRepository = liquidacionRepository;
        this.espacioRepository = espacioRepository;
        this.gastoRepository = gastoRepository;
    }

    @Override
    public LiquidacionResponseDTO procesarCierreLiquidacion(Long espacioId, ProcesarLiquidacionDTO dto) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        List< Gasto > gastosALiquidar;
        if (dto != null && dto.getGastoIds() != null && !dto.getGastoIds().isEmpty()) {
            
            List< Gasto > gastosSolicitados = gastoRepository.findAllById(dto.getGastoIds());
            
            if (gastosSolicitados.size() != dto.getGastoIds().size()) {
                throw new ReglaInvalidaException("Uno o más identificadores de gastos proporcionados no existen.");
            }

            for (Gasto g : gastosSolicitados) {
                if (!g.getEspacio().getId().equals(espacioId)) {
                    throw new ReglaInvalidaException("El gasto con id " + g.getId() + " no pertenece al espacio indicado.");
                }
                if (g.getEstado() != EstadoGasto.PENDIENTE) {
                    throw new ReglaInvalidaException("El gasto con id " + g.getId() + " ya se encuentra liquidado o en un estado inválido.");
                }
            }
            gastosALiquidar = gastosSolicitados;
        } else {
            gastosALiquidar = gastoRepository.findByEspacioIdAndEstadoOrderByFechaAsc(espacioId, EstadoGasto.PENDIENTE);
        }

        if (gastosALiquidar.isEmpty()) {
            throw new ReglaInvalidaException("No existen gastos pendientes para liquidar en el espacio");
        }

        BigDecimal totalLiquidado = BigDecimal.ZERO;
        List< Long > gastosLiquidadosIds = new ArrayList<>();
        for (Gasto gasto : gastosALiquidar) {
            totalLiquidado = totalLiquidado.add(gasto.getMonto());
            gastosLiquidadosIds.add(gasto.getId());
        }

        BigDecimal presupuestoAnterior = espacio.getPresupuestoBase() != null ? espacio.getPresupuestoBase() : BigDecimal.ZERO;
        if (presupuestoAnterior.compareTo(totalLiquidado) < 0) {
            throw new ReglaInvalidaException("El total a liquidar (" + totalLiquidado +
                    ") supera el presupuesto disponible del espacio (" + presupuestoAnterior + "). No se realizaron cambios parciales.");
        }

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

    @Override
    @Transactional(readOnly = true)
    public List< LiquidacionResponseDTO > obtenerHistorial(Long espacioId, LocalDate desde, LocalDate hasta, Integer anio, Integer mes) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }

        if ((desde != null && hasta == null) || (desde == null && hasta != null)) {
            throw new ReglaInvalidaException("Los filtros de fecha 'desde' y 'hasta' deben enviarse juntos.");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaInvalidaException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if ((anio != null && mes == null) || (anio == null && mes != null)) {
            throw new ReglaInvalidaException("Los filtros de período 'anio' y 'mes' deben enviarse juntos.");
        }
        if (desde != null && anio != null) {
            throw new ReglaInvalidaException("Filtros incompatibles: no se puede buscar por rango de fechas y por período simultáneamente.");
        }

        List< Liquidacion > liquidaciones;
        if (desde != null && hasta != null) {
            liquidaciones = liquidacionRepository.findByEspacio_IdAndFechaLiquidacionBetweenOrderByFechaLiquidacionDesc(espacioId, desde, hasta);
        } else if (anio != null && mes != null) {
            liquidaciones = liquidacionRepository.findByEspacio_IdAndAnioAndMesOrderByFechaLiquidacionDesc(espacioId, anio, mes);
        } else {
            liquidaciones = liquidacionRepository.findByEspacio_IdOrderByFechaLiquidacionDesc(espacioId);
        }

        List< LiquidacionResponseDTO > resultado = new ArrayList<>();
        for (Liquidacion l : liquidaciones) {
            resultado.add(mapearAResponseDTO(l));
        }
        return resultado;
    }

    private LiquidacionResponseDTO mapearAResponseDTO(Liquidacion l) {
        List< Long > ids = new ArrayList<>();
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