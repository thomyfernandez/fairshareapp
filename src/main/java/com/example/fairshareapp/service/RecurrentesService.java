package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.CicloVencidoException;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarPrecioCicloDTO;
import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.GastoParticipanteDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoRequestDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoResponseDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.model.entity.*;
import com.example.fairshareapp.model.enums.ReglaDivision;
import com.example.fairshareapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio de negocio para la administracion de gastos recurrentes, plantillas y favoritos.
 * Permite gestionar favoritos, detectar vencimientos de ciclos tarifarios (alquileres, IPC o servicios),
 * actualizar precios y disparar el registro de gastos en 1 solo clic a traves de GastoService.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RecurrentesService {

    private static final String PLANTILLA_NO_ENCONTRADA = "Plantilla favorita no encontrada con id: ";

    private final PlantillaGastoRepository plantillaRepository;
    private final ServicioRepository servicioRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final MiembroEspacioRepository miembroEspacioRepository;
    private final GastoService gastoService;

    /**
     * Guarda una plantilla de gasto como favorita asociada a un espacio compartido.
     *
     * @param dto Informacion de la plantilla a registrar.
     * @return DTO de la plantilla persistida.
     */
    public PlantillaGastoResponseDTO guardarFavorito(PlantillaGastoRequestDTO dto) {
        validarDatosPlantilla(dto);

        Espacio espacio = espacioRepository.findById(dto.getEspacioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con id: " + dto.getEspacioId()));

        Usuario pagador = usuarioRepository.findById(dto.getPagadorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pagador no encontrado con id: " + dto.getPagadorId()));

        if (!miembroEspacioRepository.existsByEspacioIdAndUsuarioId(espacio.getId(), pagador.getId()))
            throw new ReglaInvalidaException("El pagador debe pertenecer al espacio");
        if (dto.getReglaDivision() == ReglaDivision.PERSONALIZADA)
            throw new ReglaInvalidaException("Las plantillas admiten reparto equitativo, parcial o proporcional; el personalizado requiere cuotas por gasto");
        Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada con id: " + dto.getCategoriaId()));
        }

        Servicio servicio = null;
        if (dto.getServicioId() != null) {
            servicio = servicioRepository.findById(dto.getServicioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + dto.getServicioId()));
        }

        PlantillaGastoRecurrente plantilla = PlantillaGastoRecurrente.builder()
                .nombre(dto.getNombre().trim())
                .frecuenciaAjusteMeses(dto.getFrecuenciaAjusteMeses())
                .montoBase(dto.getMontoBase())
                .montoVariable(dto.getMontoVariable() != null ? dto.getMontoVariable() : BigDecimal.ZERO)
                .fechaProximaRevision(dto.getFechaProximaRevision())
                .espacioId(espacio)
                .servicio(servicio)
                .pagadorId(pagador)
                .categoria(categoria)
                .reglaDivision(dto.getReglaDivision() != null ? dto.getReglaDivision() : ReglaDivision.EQUITATIVA)
                .build();

        PlantillaGastoRecurrente guardada = plantillaRepository.save(plantilla);
        return mapearAPlantillaDTO(guardada);
    }

    /**
     * Elimina una plantilla favorita por su identificador unico.
     *
     * @param id Identificador unico de la plantilla a eliminar.
     */
    public void eliminarFavorito(Long id) {
        if (!plantillaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(PLANTILLA_NO_ENCONTRADA + id);
        }
        plantillaRepository.deleteById(id);
    }

    /**
     * Consulta las plantillas de gastos registradas en un espacio determinado, opcionalmente
     * filtrando solo aquellas cuyo ciclo de revision se encuentra vencido.
     *
     * @param espacioId Identificador del espacio a consultar.
     * @param soloVencidos Si es true, restringe el resultado a plantillas con ciclo vencido.
     * @return Lista de plantillas favoritas del espacio segun el criterio solicitado.
     */
    @Transactional(readOnly = true)
    public List<PlantillaGastoResponseDTO> obtenerFavoritosPorEspacio(Long espacioId, boolean soloVencidos) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }

        List<PlantillaGastoRecurrente> plantillas = soloVencidos
                ? plantillaRepository.findVencidasPorEspacio(espacioId, LocalDate.now(ZoneId.systemDefault()))
                : plantillaRepository.findByEspacioId(espacioId);

        return plantillas.stream()
                .map(this::mapearAPlantillaDTO)
                .toList();
    }

    /**
     * Consulta una plantilla de gasto especifica a partir de su identificador.
     *
     * @param id Identificador de la plantilla.
     * @return DTO de la plantilla encontrada.
     */
    @Transactional(readOnly = true)
    public PlantillaGastoResponseDTO obtenerFavoritoPorId(Long id) {
        PlantillaGastoRecurrente plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PLANTILLA_NO_ENCONTRADA + id));
        return mapearAPlantillaDTO(plantilla);
    }

    /**
     * Detecta vencimientos de ciclo tarifario en las plantillas de un espacio comparando la fecha actual.
     *
     * @param espacioId Identificador del espacio a evaluar.
     * @return Lista de vencimientos detectados que requieren revision o ajuste de tarifa.
     */
    @Transactional(readOnly = true)
    public List<ServicioVencimientoDTO> detectarVencimientos(Long espacioId) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }

        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        List<PlantillaGastoRecurrente> vencidas = plantillaRepository.findVencidasPorEspacio(espacioId, hoy);

        return vencidas.stream()
                .map(p -> {
                    long dias = ChronoUnit.DAYS.between(p.getFechaProximaRevision(), hoy);
                    Long servicioIdRef = p.getServicio() != null ? p.getServicio().getId() : p.getId();
                    String nombreRef = p.getServicio() != null ? p.getServicio().getNombre() : p.getNombre();

                    return ServicioVencimientoDTO.builder()
                            .servicioId(servicioIdRef)
                            .nombreServicio(nombreRef)
                            .fechaVencimiento(p.getFechaProximaRevision())
                            .montoBase(p.getMontoBase())
                            .montoVariable(p.getMontoVariable())
                            .diasVencido(dias)
                            .build();
                })
                .toList();
    }

    /**
     * Actualiza la tarifa o montos base/variable del ciclo de una plantilla y reprograma la revision.
     *
     * @param id Identificador de la plantilla a actualizar.
     * @param dto Nuevos importes y fecha de revision opcional.
     * @return Plantilla con los importes y fecha de ciclo renovados.
     */
    public PlantillaGastoResponseDTO actualizarMonto(Long id, ActualizarPrecioCicloDTO dto) {
        if (dto == null) {
            throw new ReglaInvalidaException("Los datos de actualizacion no pueden ser nulos");
        }
        if (dto.getNuevoMontoBase() == null || dto.getNuevoMontoBase().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El nuevo monto base debe ser mayor o igual a cero");
        }
        if (dto.getNuevoMontoVariable() != null && dto.getNuevoMontoVariable().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El nuevo monto variable no puede ser negativo");
        }

        PlantillaGastoRecurrente plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PLANTILLA_NO_ENCONTRADA + id));

        if (dto.getNuevaFechaProximaRevision() != null && !dto.getNuevaFechaProximaRevision().isAfter(LocalDate.now()))
            throw new ReglaInvalidaException("La nueva revisión debe ser posterior a hoy");
        if (dto.getNuevaFechaProximaRevision() == null && (plantilla.getFrecuenciaAjusteMeses() == null || plantilla.getFrecuenciaAjusteMeses() < 1))
            throw new ReglaInvalidaException("Indique la nueva fecha de revisión o configure una frecuencia");
        plantilla.setMontoBase(dto.getNuevoMontoBase());
        if (dto.getNuevoMontoVariable() != null) {
            plantilla.setMontoVariable(dto.getNuevoMontoVariable());
        }

        if (dto.getNuevaFechaProximaRevision() != null) {
            plantilla.setFechaProximaRevision(dto.getNuevaFechaProximaRevision());
        } else if (plantilla.getFrecuenciaAjusteMeses() != null && plantilla.getFrecuenciaAjusteMeses() > 0) {
            LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
            LocalDate fechaBase = (plantilla.getFechaProximaRevision() != null && plantilla.getFechaProximaRevision().isAfter(hoy))
                    ? plantilla.getFechaProximaRevision()
                    : hoy;
            plantilla.setFechaProximaRevision(fechaBase.plusMonths(plantilla.getFrecuenciaAjusteMeses()));
        }

        PlantillaGastoRecurrente actualizada = plantillaRepository.save(plantilla);
        return mapearAPlantillaDTO(actualizada);
    }

    /**
     * Dispara la creacion de un gasto real en 1 solo clic a partir de los datos configurados en la plantilla,
     * reutilizando GastoService para persistirlo con sus mismas validaciones de negocio.
     * Exige previamente la actualizacion de tarifa si el ciclo de revision se encuentra vencido, y que tanto
     * el pagador configurado como los participantes sean miembros vigentes del espacio de la plantilla.
     *
     * @param id Identificador de la plantilla a ejecutar.
     * @return Detalle del gasto registrado en el espacio.
     */
    public GastoDetalleDTO ejecutarGastoDesdePlantilla(Long id) {
        PlantillaGastoRecurrente plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PLANTILLA_NO_ENCONTRADA + id));

        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        if (plantilla.getFechaProximaRevision() != null && !plantilla.getFechaProximaRevision().isAfter(hoy)) {
            throw new CicloVencidoException(plantilla.getNombre(), plantilla.getFechaProximaRevision());
        }

        BigDecimal montoTotal = BigDecimal.ZERO;
        if (plantilla.getMontoBase() != null) {
            montoTotal = montoTotal.add(plantilla.getMontoBase());
        }
        if (plantilla.getMontoVariable() != null) {
            montoTotal = montoTotal.add(plantilla.getMontoVariable());
        }
        if (montoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaInvalidaException("El monto total a debitar debe ser un valor positivo");
        }

        Long espacioId = plantilla.getEspacioId() != null ? plantilla.getEspacioId().getId() : null;
        if (espacioId == null) {
            throw new ReglaInvalidaException("La plantilla no tiene un espacio asociado");
        }

        List<MiembroEspacio> miembros = miembroEspacioRepository.findByEspacioId(espacioId);
        if (miembros.isEmpty()) {
            throw new ReglaInvalidaException("El espacio no tiene miembros para generar el gasto");
        }

        Long pagadorId = plantilla.getPagadorId() != null ? plantilla.getPagadorId().getId() : null;
        if (pagadorId == null) {
            throw new ReglaInvalidaException("La plantilla no tiene un pagador configurado");
        }
        if (!miembroEspacioRepository.existsByEspacioIdAndUsuarioId(espacioId, pagadorId)) {
            throw new ReglaInvalidaException("El pagador configurado en la plantilla ya no pertenece al espacio");
        }

        List<GastoParticipanteDTO> participantes = miembros.stream()
                .map(m -> GastoParticipanteDTO.builder().usuarioId(m.getUsuario().getId()).build())
                .toList();

        ReglaDivision regla = plantilla.getReglaDivision() != null ? plantilla.getReglaDivision() : ReglaDivision.EQUITATIVA;
        Long categoriaId = plantilla.getCategoria() != null ? plantilla.getCategoria().getId() : null;

        CrearGastoDTO crearGastoDTO = CrearGastoDTO.builder()
                .descripcion(plantilla.getNombre())
                .monto(montoTotal)
                .fecha(hoy)
                .pagadorId(pagadorId)
                .categoriaId(categoriaId)
                .regla(regla)
                .participantes(participantes)
                .build();

        return gastoService.registrarGasto(espacioId, crearGastoDTO);
    }

    /**
     * Valida los campos obligatorios para registrar una nueva plantilla.
     *
     * @param dto Informacion de la plantilla a validar.
     */
    private void validarDatosPlantilla(PlantillaGastoRequestDTO dto) {
        if (dto == null) {
            throw new ReglaInvalidaException("La plantilla de gasto no puede ser nula");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ReglaInvalidaException("El nombre de la plantilla es obligatorio");
        }
        if (dto.getEspacioId() == null) {
            throw new ReglaInvalidaException("El identificador del espacio es obligatorio");
        }
        if (dto.getPagadorId() == null) {
            throw new ReglaInvalidaException("El identificador del pagador es obligatorio");
        }
        if (dto.getMontoBase() == null || dto.getMontoBase().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El monto base debe ser mayor o igual a cero");
        }
        if (dto.getMontoVariable() != null && dto.getMontoVariable().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El monto variable no puede ser negativo");
        }
        if (dto.getFechaProximaRevision() == null) {
            throw new ReglaInvalidaException("La fecha de proxima revision del ciclo es obligatoria");
        }
    }

    /**
     * Mapea una entidad PlantillaGastoRecurrente a su correspondiente DTO de salida.
     *
     * @param plantilla Entidad a transformar.
     * @return DTO resultante con el estado de vencimiento evaluado.
     */
    private PlantillaGastoResponseDTO mapearAPlantillaDTO(PlantillaGastoRecurrente plantilla) {
        boolean vencido = plantilla.getFechaProximaRevision() != null
                && !plantilla.getFechaProximaRevision().isAfter(LocalDate.now(ZoneId.systemDefault()));

        return PlantillaGastoResponseDTO.builder()
                .id(plantilla.getId())
                .nombre(plantilla.getNombre())
                .frecuenciaAjusteMeses(plantilla.getFrecuenciaAjusteMeses())
                .montoBase(plantilla.getMontoBase())
                .montoVariable(plantilla.getMontoVariable())
                .fechaProximaRevision(plantilla.getFechaProximaRevision())
                .espacioId(plantilla.getEspacioId() != null ? plantilla.getEspacioId().getId() : null)
                .servicioId(plantilla.getServicio() != null ? plantilla.getServicio().getId() : null)
                .nombreServicio(plantilla.getServicio() != null ? plantilla.getServicio().getNombre() : null)
                .pagadorId(plantilla.getPagadorId() != null ? plantilla.getPagadorId().getId() : null)
                .categoriaId(plantilla.getCategoria() != null ? plantilla.getCategoria().getId() : null)
                .reglaDivision(plantilla.getReglaDivision())
                .vencido(vencido)
                .build();
    }
}
