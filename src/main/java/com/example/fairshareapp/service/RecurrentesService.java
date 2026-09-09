package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarPrecioCicloDTO;
import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.GastoParticipanteDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import com.example.fairshareapp.model.entity.ReglaDivision;
import com.example.fairshareapp.model.entity.Servicio;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.PlantillaGastoRepository;
import com.example.fairshareapp.repository.ServicioRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de negocio para la administracion de gastos recurrentes, plantillas y favoritos.
 * Permite gestionar favoritos, detectar vencimientos de ciclos tarifarios (alquileres, IPC o servicios),
 * actualizar precios y disparar el registro de gastos en 1 solo clic.
 */
@Service
@Transactional
public class RecurrentesService {

    private static final String PLANTILLA_NO_ENCONTRADA = "Plantilla favorita no encontrada con id: ";

    private final PlantillaGastoRepository plantillaRepository;
    private final ServicioRepository servicioRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final GastoService gastoService;

    /**
     * Constructor con inyeccion de dependencias de los componentes requeridos.
     *
     * @param plantillaRepository Repositorio de plantillas de gastos.
     * @param servicioRepository Repositorio de catalogo de servicios.
     * @param espacioRepository Repositorio de espacios compartidos.
     * @param usuarioRepository Repositorio de usuarios.
     * @param gastoService Servicio de logica de gastos.
     */
    public RecurrentesService(PlantillaGastoRepository plantillaRepository,
                              ServicioRepository servicioRepository,
                              EspacioRepository espacioRepository,
                              UsuarioRepository usuarioRepository,
                              GastoService gastoService) {
        this.plantillaRepository = plantillaRepository;
        this.servicioRepository = servicioRepository;
        this.espacioRepository = espacioRepository;
        this.usuarioRepository = usuarioRepository;
        this.gastoService = gastoService;
    }

    /**
     * Guarda una plantilla de gasto como favorita asociada a un espacio compartido.
     *
     * @param dto Informacion de la plantilla a registrar.
     * @return DTO de la plantilla persistida.
     */
    public PlantillaGastoDTO guardarFavorito(PlantillaGastoDTO dto) {
        validarDatosPlantilla(dto);

        if (!espacioRepository.existsById(dto.getEspacioId())) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + dto.getEspacioId());
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
                .espacioId(dto.getEspacioId())
                .servicio(servicio)
                .pagadorId(dto.getPagadorId())
                .categoriaId(dto.getCategoriaId())
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
     * Consulta las plantillas de gastos registradas en un espacio determinado.
     *
     * @param espacioId Identificador del espacio a consultar.
     * @return Lista de plantillas favoritas del espacio.
     */
    @Transactional(readOnly = true)
    public List<PlantillaGastoDTO> obtenerFavoritosPorEspacio(Long espacioId) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new RecursoNoEncontradoException("Espacio no encontrado con id: " + espacioId);
        }

        return plantillaRepository.findByEspacioId(espacioId).stream()
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
    public PlantillaGastoDTO obtenerFavoritoPorId(Long id) {
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

        List<PlantillaGastoRecurrente> plantillas = plantillaRepository.findByEspacioId(espacioId);
        List<ServicioVencimientoDTO> vencidos = new ArrayList<>();
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());

        for (PlantillaGastoRecurrente p : plantillas) {
            if (p.getFechaProximaRevision() != null && !p.getFechaProximaRevision().isAfter(hoy)) {
                long dias = ChronoUnit.DAYS.between(p.getFechaProximaRevision(), hoy);
                Long servicioIdRef = p.getServicio() != null ? p.getServicio().getId() : p.getId();
                String nombreRef = p.getServicio() != null ? p.getServicio().getNombre() : p.getNombre();

                ServicioVencimientoDTO dto = ServicioVencimientoDTO.builder()
                        .servicioId(servicioIdRef)
                        .nombreServicio(nombreRef)
                        .fechaVencimiento(p.getFechaProximaRevision())
                        .montoBase(p.getMontoBase())
                        .montoVariable(p.getMontoVariable())
                        .diasVencido(dias)
                        .build();
                vencidos.add(dto);
            }
        }
        return vencidos;
    }

    /**
     * Actualiza la tarifa o montos base/variable del ciclo de una plantilla y reprograma la revision.
     *
     * @param id Identificador de la plantilla a actualizar.
     * @param dto Nuevos importes y fecha de revision opcional.
     * @return Plantilla con los importes y fecha de ciclo renovados.
     */
    public PlantillaGastoDTO actualizarMonto(Long id, ActualizarPrecioCicloDTO dto) {
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
     * Dispara la creacion de un gasto real en 1 solo clic a partir de los datos configurados en la plantilla.
     * Exige previamente la actualizacion de tarifa si el ciclo de revision se encuentra vencido.
     *
     * @param id Identificador de la plantilla a ejecutar.
     * @return Detalle del gasto registrado en el espacio.
     */
    public GastoDetalleDTO ejecutarGastoDesdePlantilla(Long id) {
        PlantillaGastoRecurrente plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PLANTILLA_NO_ENCONTRADA + id));

        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        if (plantilla.getFechaProximaRevision() != null && !plantilla.getFechaProximaRevision().isAfter(hoy)) {
            throw new ReglaInvalidaException("El ciclo de la plantilla '" + plantilla.getNombre()
                    + "' vencio el " + plantilla.getFechaProximaRevision()
                    + ". Debe actualizar la tarifa antes de ejecutar el gasto.");
        }

        BigDecimal montoTotalBD = BigDecimal.ZERO;
        if (plantilla.getMontoBase() != null) {
            montoTotalBD = montoTotalBD.add(plantilla.getMontoBase());
        }
        if (plantilla.getMontoVariable() != null) {
            montoTotalBD = montoTotalBD.add(plantilla.getMontoVariable());
        }

        if (montoTotalBD.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaInvalidaException("El monto total a debitar debe ser un valor positivo");
        }

        List<Usuario> usuariosDisponibles = usuarioRepository.findAll();
        if (usuariosDisponibles.isEmpty()) {
            throw new ReglaInvalidaException("No hay usuarios registrados en el sistema para asociar al gasto");
        }

        Long pagadorId = plantilla.getPagadorId();
        if (pagadorId == null || !usuarioRepository.existsById(pagadorId)) {
            pagadorId = usuariosDisponibles.get(0).getId();
        }

        List<GastoParticipanteDTO> participantes = usuariosDisponibles.stream()
                .map(u -> GastoParticipanteDTO.builder().usuarioId(u.getId()).build())
                .toList();

        ReglaDivision regla = plantilla.getReglaDivision() != null ? plantilla.getReglaDivision() : ReglaDivision.EQUITATIVA;

        CrearGastoDTO crearGastoDTO = CrearGastoDTO.builder()
                .descripcion(plantilla.getNombre())
                .monto(montoTotalBD.doubleValue())
                .fecha(hoy)
                .pagadorId(pagadorId)
                .categoriaId(plantilla.getCategoriaId())
                .regla(regla)
                .participantes(participantes)
                .build();

        return gastoService.registrarGasto(plantilla.getEspacioId(), crearGastoDTO);
    }

    /**
     * Valida los campos obligatorios para registrar una nueva plantilla.
     *
     * @param dto Informacion de la plantilla a validar.
     */
    private void validarDatosPlantilla(PlantillaGastoDTO dto) {
        if (dto == null) {
            throw new ReglaInvalidaException("La plantilla de gasto no puede ser nula");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ReglaInvalidaException("El nombre de la plantilla es obligatorio");
        }
        if (dto.getEspacioId() == null) {
            throw new ReglaInvalidaException("El identificador del espacio es obligatorio");
        }
        if (dto.getMontoBase() == null || dto.getMontoBase().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El monto base debe ser mayor o igual a cero");
        }
        if (dto.getMontoVariable() != null && dto.getMontoVariable().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El monto variable no puede ser negativo");
        }
    }

    /**
     * Mapea una entidad PlantillaGastoRecurrente a su correspondiente DTO.
     *
     * @param plantilla Entidad a transformar.
     * @return DTO resultante con el estado de vencimiento evaluado.
     */
    private PlantillaGastoDTO mapearAPlantillaDTO(PlantillaGastoRecurrente plantilla) {
        boolean vencido = plantilla.getFechaProximaRevision() != null
                && !plantilla.getFechaProximaRevision().isAfter(LocalDate.now(ZoneId.systemDefault()));

        return PlantillaGastoDTO.builder()
                .id(plantilla.getId())
                .nombre(plantilla.getNombre())
                .frecuenciaAjusteMeses(plantilla.getFrecuenciaAjusteMeses())
                .montoBase(plantilla.getMontoBase())
                .montoVariable(plantilla.getMontoVariable())
                .fechaProximaRevision(plantilla.getFechaProximaRevision())
                .espacioId(plantilla.getEspacioId())
                .servicioId(plantilla.getServicio() != null ? plantilla.getServicio().getId() : null)
                .nombreServicio(plantilla.getServicio() != null ? plantilla.getServicio().getNombre() : null)
                .pagadorId(plantilla.getPagadorId())
                .categoriaId(plantilla.getCategoriaId())
                .reglaDivision(plantilla.getReglaDivision())
                .vencido(vencido)
                .build();
    }
}
