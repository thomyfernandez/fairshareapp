package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.GastoParticipanteDTO;
import com.example.fairshareapp.model.entity.Categoria;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.entity.GastoParticipante;
import com.example.fairshareapp.model.entity.ReglaDivision;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.CategoriaRepository;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de negocio para la gestion y particion de gastos en la aplicacion.
 * Implementa la logica transaccional para registrar egresos individuales o por lote,
 * asignar participantes y calcular los importes segun la regla de division activa.
 */
@Service
@Transactional
public class GastoService {

    private final GastoRepository gastoRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * Constructor con inyeccion de dependencias de los repositorios requeridos.
     *
     * @param gastoRepository Repositorio de persistencia de gastos.
     * @param espacioRepository Repositorio de persistencia de espacios.
     * @param usuarioRepository Repositorio de persistencia de usuarios.
     * @param categoriaRepository Repositorio de persistencia de categorias.
     */
    public GastoService(GastoRepository gastoRepository,
                        EspacioRepository espacioRepository,
                        UsuarioRepository usuarioRepository,
                        CategoriaRepository categoriaRepository) {
        this.gastoRepository = gastoRepository;
        this.espacioRepository = espacioRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Registra un egreso en un espacio determinado calculando las cuotas de cada participante
     * segun la regla de division activa configurada.
     *
     * @param espacioId Identificador unico del espacio.
     * @param dto Datos de creacion del gasto y sus participantes.
     * @return Detalle estructurado del gasto registrado.
     */
    public GastoDetalleDTO registrarGasto(Long espacioId, CrearGastoDTO dto) {
        validarDatosCreacion(dto);

        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId));

        Usuario pagador = usuarioRepository.findById(dto.getPagadorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario pagador no encontrado con id: " + dto.getPagadorId()));

        Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada con id: " + dto.getCategoriaId()));
        }

        LocalDate fechaGasto = dto.getFecha() != null ? dto.getFecha() : LocalDate.now();
        ReglaDivision regla = dto.getRegla() != null ? dto.getRegla() : ReglaDivision.EQUITATIVA;

        Gasto nuevoGasto = Gasto.builder()
                .descripcion(dto.getDescripcion().trim())
                .monto(dto.getMonto())
                .fecha(fechaGasto)
                .espacio(espacio)
                .pagador(pagador)
                .categoria(categoria)
                .reglaDivision(regla)
                .participantes(new ArrayList<>())
                .build();

        List<GastoParticipante> participantesCalculados = calcularParticipaciones(nuevoGasto, dto.getMonto(), regla, dto.getParticipantes());
        participantesCalculados.forEach(nuevoGasto::agregarParticipante);

        Gasto gastoGuardado = gastoRepository.save(nuevoGasto);
        return mapearADetalleDTO(gastoGuardado);
    }

    /**
     * Registra un lote de gastos para un espacio en una unica transaccion atomica.
     *
     * @param espacioId Identificador del espacio.
     * @param dtos Lista de gastos a registrar.
     * @return Lista con los detalles de los gastos creados.
     */
    public List<GastoDetalleDTO> registrarLoteGastos(Long espacioId, List<CrearGastoDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new ReglaInvalidaException("El lote de gastos no contiene elementos para registrar");
        }

        List<GastoDetalleDTO> resultado = new ArrayList<>();
        for (CrearGastoDTO dto : dtos) {
            resultado.add(registrarGasto(espacioId, dto));
        }
        return resultado;
    }

    /**
     * Consulta los gastos asociados a un espacio, permitiendo filtrar por un rango de fechas.
     * Si no se especifican fechas, recupera la totalidad de gastos del espacio.
     *
     * @param espacioId Identificador del espacio.
     * @param fechaDesde Fecha inicial del periodo (opcional).
     * @param fechaHasta Fecha final del periodo (opcional).
     * @return Lista de gastos mapeados a su DTO de detalle.
     */
    @Transactional(readOnly = true)
    public List<GastoDetalleDTO> obtenerGastosPorEspacioYPeriodo(Long espacioId, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }

        List<Gasto> gastos;
        if (fechaDesde != null && fechaHasta != null) {
            if (fechaDesde.isAfter(fechaHasta)) {
                throw new ReglaInvalidaException("La fecha desde no puede ser posterior a la fecha hasta");
            }
            gastos = gastoRepository.findByEspacioIdAndFechaBetweenOrderByFechaDesc(espacioId, fechaDesde, fechaHasta);
        } else {
            gastos = gastoRepository.findByEspacioIdOrderByFechaDesc(espacioId);
        }

        return gastos.stream().map(this::mapearADetalleDTO).toList();
    }

    /**
     * Recupera el detalle completo de un gasto a traves de su identificador unico.
     *
     * @param gastoId Identificador del gasto a buscar.
     * @return Detalle del gasto encontrado.
     */
    @Transactional(readOnly = true)
    public GastoDetalleDTO obtenerGastoPorId(Long gastoId) {
        Gasto gasto = gastoRepository.findById(gastoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Gasto no encontrado con id: " + gastoId));
        return mapearADetalleDTO(gasto);
    }

    /**
     * Elimina un gasto del sistema y sus participaciones asociadas en cascada.
     *
     * @param gastoId Identificador del gasto a eliminar.
     */
    public void eliminarGasto(Long gastoId) {
        if (!gastoRepository.existsById(gastoId)) {
            throw new RecursoNoEncontradoException("Gasto no encontrado con id: " + gastoId);
        }
        gastoRepository.deleteById(gastoId);
    }

    /**
     * Valida los campos obligatorios y coherencia general del DTO de creacion de gasto.
     *
     * @param dto DTO con los datos del gasto a validar.
     */
    private void validarDatosCreacion(CrearGastoDTO dto) {
        if (dto == null) {
            throw new ReglaInvalidaException("El cuerpo de la solicitud no puede ser nulo");
        }
        if (dto.getDescripcion() == null || dto.getDescripcion().trim().isEmpty()) {
            throw new ReglaInvalidaException("La descripcion del gasto es obligatoria");
        }
        if (dto.getMonto() == null || dto.getMonto() <= 0) {
            throw new ReglaInvalidaException("El monto del gasto debe ser un valor positivo");
        }
        if (dto.getPagadorId() == null) {
            throw new ReglaInvalidaException("Debe especificarse el id del usuario pagador");
        }
        if (dto.getParticipantes() == null || dto.getParticipantes().isEmpty()) {
            throw new ReglaInvalidaException("Debe incluir al menos un participante para la division del gasto");
        }
    }

    /**
     * Realiza el calculo de importes y porcentajes de participacion segun la regla de division activa.
     *
     * @param gasto Instancia de gasto a asociar.
     * @param montoTotal Monto total del egreso a dividir.
     * @param regla Regla activa seleccionada para la particion.
     * @param participantesDTO Lista de participantes con su informacion.
     * @return Lista de entidades GastoParticipante calculadas y preparadas para persistir.
     */
    private List<GastoParticipante> calcularParticipaciones(Gasto gasto,
                                                          Double montoTotal,
                                                          ReglaDivision regla,
                                                          List<GastoParticipanteDTO> participantesDTO) {
        List<Usuario> usuarios = new ArrayList<>();
        for (GastoParticipanteDTO partDto : participantesDTO) {
            if (partDto.getUsuarioId() == null) {
                throw new ReglaInvalidaException("Cada participante debe indicar un usuarioId valido");
            }
            Usuario usuario = usuarioRepository.findById(partDto.getUsuarioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario participante no encontrado con id: " + partDto.getUsuarioId()));
            usuarios.add(usuario);
        }

        return switch (regla) {
            case EQUITATIVA, PARTICIPACION_PARCIAL -> calcularDivisionEquitativa(gasto, montoTotal, usuarios);
            case PROPORCIONAL_INGRESOS -> calcularDivisionProporcionalIngresos(gasto, montoTotal, usuarios, participantesDTO);
            case PERSONALIZADA -> calcularDivisionPersonalizada(gasto, montoTotal, usuarios, participantesDTO);
        };
    }

    /**
     * Calcula la division en partes iguales para una lista de usuarios participantes,
     * compensando cualquier discrepancia de redondeo de centavos en la primera cuota.
     *
     * @param gasto Entidad Gasto asociada.
     * @param montoTotal Monto total a dividir.
     * @param usuarios Lista de usuarios que participan de la division.
     * @return Lista de participaciones con sus cuotas equitativas asignadas.
     */
    private List<GastoParticipante> calcularDivisionEquitativa(Gasto gasto, Double montoTotal, List<Usuario> usuarios) {
        int cantidad = usuarios.size();
        BigDecimal total = BigDecimal.valueOf(montoTotal);
        BigDecimal divisor = BigDecimal.valueOf(cantidad);

        // Cuota base redondeada hacia abajo a 2 decimales
        BigDecimal cuotaBase = total.divide(divisor, 2, RoundingMode.DOWN);
        BigDecimal sumaParcial = cuotaBase.multiply(divisor);
        BigDecimal restoCentavos = total.subtract(sumaParcial);

        BigDecimal porcentajePorPersona = BigDecimal.valueOf(100.0)
                .divide(divisor, 2, RoundingMode.HALF_UP);

        List<GastoParticipante> resultado = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            // Se asigna la diferencia residual de centavos al primer participante para cuadrar el monto total
            BigDecimal cuotaFinal = (i == 0) ? cuotaBase.add(restoCentavos) : cuotaBase;

            resultado.add(GastoParticipante.builder()
                    .gasto(gasto)
                    .usuario(usuarios.get(i))
                    .importe(cuotaFinal.doubleValue())
                    .porcentaje(porcentajePorPersona.doubleValue())
                    .build());
        }
        return resultado;
    }

    /**
     * Calcula la particion proporcional a los ingresos o sueldos mensuales declarados,
     * asegurando que a todos los miembros les quede identico porcentaje de ingreso libre.
     *
     * @param gasto Entidad Gasto asociada.
     * @param montoTotal Monto total del gasto.
     * @param usuarios Lista de usuarios participantes.
     * @param dtos Lista de DTOs participantes para soporte de ingresos especificos.
     * @return Lista de participaciones ponderadas por ingreso.
     */
    private List<GastoParticipante> calcularDivisionProporcionalIngresos(Gasto gasto,
                                                                        Double montoTotal,
                                                                        List<Usuario> usuarios,
                                                                        List<GastoParticipanteDTO> dtos) {
        List<BigDecimal> sueldos = new ArrayList<>();
        BigDecimal totalSueldos = BigDecimal.ZERO;

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario usuario = usuarios.get(i);
            GastoParticipanteDTO partDto = dtos.get(i);

            // Permite tomar el sueldo provisto en la solicitud o el persistido en la entidad de usuario
            Double sueldoDeclarado = (partDto.getSueldo() != null && partDto.getSueldo() > 0)
                    ? partDto.getSueldo()
                    : usuario.getSueldo();

            if (sueldoDeclarado == null || sueldoDeclarado <= 0) {
                throw new ReglaInvalidaException("El usuario con id " + usuario.getId() +
                        " no cuenta con un sueldo registrado mayor a cero para aplicar la division proporcional");
            }

            BigDecimal sueldoBD = BigDecimal.valueOf(sueldoDeclarado);
            sueldos.add(sueldoBD);
            totalSueldos = totalSueldos.add(sueldoBD);
        }

        BigDecimal montoTotalBD = BigDecimal.valueOf(montoTotal);
        BigDecimal acumuladoImportes = BigDecimal.ZERO;
        List<GastoParticipante> participaciones = new ArrayList<>();

        for (int i = 0; i < usuarios.size(); i++) {
            BigDecimal sueldo = sueldos.get(i);
            // Multiplica monto por sueldo antes de dividir por totalSueldos para mantener maxima precision
            BigDecimal importe = montoTotalBD.multiply(sueldo).divide(totalSueldos, 2, RoundingMode.HALF_UP);
            BigDecimal porcentaje = sueldo.multiply(BigDecimal.valueOf(100.0)).divide(totalSueldos, 2, RoundingMode.HALF_UP);

            acumuladoImportes = acumuladoImportes.add(importe);
            participaciones.add(GastoParticipante.builder()
                    .gasto(gasto)
                    .usuario(usuarios.get(i))
                    .importe(importe.doubleValue())
                    .porcentaje(porcentaje.doubleValue())
                    .build());
        }

        // Ajuste de eventuales centavos de redondeo sobre el primer participante para balance exacto
        BigDecimal diferencia = montoTotalBD.subtract(acumuladoImportes);
        if (diferencia.compareTo(BigDecimal.ZERO) != 0 && !participaciones.isEmpty()) {
            GastoParticipante primerParticipante = participaciones.get(0);
            BigDecimal importeCorregido = BigDecimal.valueOf(primerParticipante.getImporte()).add(diferencia);
            primerParticipante.setImporte(importeCorregido.doubleValue());
        }

        return participaciones;
    }

    /**
     * Calcula o valida particiones personalizadas basadas en porcentajes manuales
     * o montos fijos asignados a cada participante.
     *
     * @param gasto Entidad Gasto asociada.
     * @param montoTotal Monto total del gasto.
     * @param usuarios Lista de usuarios participantes.
     * @param dtos Lista de DTOs con los porcentajes o montos definidos manualmente.
     * @return Lista de participaciones personalizadas validadas.
     */
    private List<GastoParticipante> calcularDivisionPersonalizada(Gasto gasto,
                                                                 Double montoTotal,
                                                                 List<Usuario> usuarios,
                                                                 List<GastoParticipanteDTO> dtos) {
        boolean usaPorcentajes = dtos.stream().anyMatch(d -> d.getPorcentaje() != null);
        boolean usaImportes = dtos.stream().anyMatch(d -> d.getImporte() != null);

        if (!usaPorcentajes && !usaImportes) {
            throw new ReglaInvalidaException("Para la regla personalizada se requiere indicar porcentajes o montos por participante");
        }

        BigDecimal montoTotalBD = BigDecimal.valueOf(montoTotal);
        List<GastoParticipante> resultado = new ArrayList<>();

        if (usaPorcentajes) {
            BigDecimal sumaPorcentajes = BigDecimal.ZERO;
            for (GastoParticipanteDTO dto : dtos) {
                if (dto.getPorcentaje() == null || dto.getPorcentaje() < 0) {
                    throw new ReglaInvalidaException("Cada participante debe contar con un porcentaje valido no negativo");
                }
                sumaPorcentajes = sumaPorcentajes.add(BigDecimal.valueOf(dto.getPorcentaje()));
            }

            // Validar que la suma de porcentajes coincida con el 100% con tolerancia minima
            if (sumaPorcentajes.subtract(BigDecimal.valueOf(100.0)).abs().compareTo(BigDecimal.valueOf(0.01)) > 0) {
                throw new ReglaInvalidaException("La suma de porcentajes debe ser exactamente 100%. Suma actual: " + sumaPorcentajes);
            }

            BigDecimal sumaImportes = BigDecimal.ZERO;
            for (int i = 0; i < usuarios.size(); i++) {
                BigDecimal porcentaje = BigDecimal.valueOf(dtos.get(i).getPorcentaje());
                BigDecimal importe = montoTotalBD.multiply(porcentaje)
                        .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);

                sumaImportes = sumaImportes.add(importe);
                resultado.add(GastoParticipante.builder()
                        .gasto(gasto)
                        .usuario(usuarios.get(i))
                        .importe(importe.doubleValue())
                        .porcentaje(porcentaje.doubleValue())
                        .build());
            }

            BigDecimal diferencia = montoTotalBD.subtract(sumaImportes);
            if (diferencia.compareTo(BigDecimal.ZERO) != 0 && !resultado.isEmpty()) {
                GastoParticipante primerParticipante = resultado.get(0);
                primerParticipante.setImporte(BigDecimal.valueOf(primerParticipante.getImporte()).add(diferencia).doubleValue());
            }

        } else {
            BigDecimal sumaImportes = BigDecimal.ZERO;
            for (GastoParticipanteDTO dto : dtos) {
                if (dto.getImporte() == null || dto.getImporte() < 0) {
                    throw new ReglaInvalidaException("Cada participante debe contar con un importe fijo valido no negativo");
                }
                sumaImportes = sumaImportes.add(BigDecimal.valueOf(dto.getImporte()));
            }

            // Validar que la suma de importes equivalga exactamente al total
            if (sumaImportes.subtract(montoTotalBD).abs().compareTo(BigDecimal.valueOf(0.01)) > 0) {
                throw new ReglaInvalidaException("La suma de importes fijados (" + sumaImportes +
                        ") no coincide con el monto total (" + montoTotalBD + ")");
            }

            for (int i = 0; i < usuarios.size(); i++) {
                BigDecimal importe = BigDecimal.valueOf(dtos.get(i).getImporte());
                BigDecimal porcentaje = importe.multiply(BigDecimal.valueOf(100.0))
                        .divide(montoTotalBD, 2, RoundingMode.HALF_UP);

                resultado.add(GastoParticipante.builder()
                        .gasto(gasto)
                        .usuario(usuarios.get(i))
                        .importe(importe.doubleValue())
                        .porcentaje(porcentaje.doubleValue())
                        .build());
            }
        }

        return resultado;
    }

    /**
     * Mapea una entidad Gasto a su correspondiente representacion DTO GastoDetalleDTO.
     *
     * @param gasto Entidad de gasto a transformar.
     * @return DTO con todos los datos y participantes del gasto.
     */
    private GastoDetalleDTO mapearADetalleDTO(Gasto gasto) {
        List<GastoParticipanteDTO> participantesDTO = gasto.getParticipantes().stream()
                .map(p -> GastoParticipanteDTO.builder()
                        .id(p.getId())
                        .usuarioId(p.getUsuario().getId())
                        .usuarioNombre(p.getUsuario().getNombre() + " " + p.getUsuario().getApellido())
                        .usuarioEmail(p.getUsuario().getEmail())
                        .importe(p.getImporte())
                        .porcentaje(p.getPorcentaje())
                        .sueldo(p.getUsuario().getSueldo())
                        .build())
                .toList();

        return GastoDetalleDTO.builder()
                .id(gasto.getId())
                .descripcion(gasto.getDescripcion())
                .monto(gasto.getMonto())
                .fecha(gasto.getFecha())
                .espacioId(gasto.getEspacio() != null ? gasto.getEspacio().getId() : null)
                .espacioNombre(gasto.getEspacio() != null ? gasto.getEspacio().getNombre() : null)
                .pagadorId(gasto.getPagador() != null ? gasto.getPagador().getId() : null)
                .pagadorNombre(gasto.getPagador() != null
                        ? gasto.getPagador().getNombre() + " " + gasto.getPagador().getApellido()
                        : null)
                .pagadorEmail(gasto.getPagador() != null ? gasto.getPagador().getEmail() : null)
                .categoriaId(gasto.getCategoria() != null ? gasto.getCategoria().getId() : null)
                .categoriaNombre(gasto.getCategoria() != null ? gasto.getCategoria().getNombre() : null)
                .regla(gasto.getReglaDivision())
                .participantes(participantesDTO)
                .build();
    }
}