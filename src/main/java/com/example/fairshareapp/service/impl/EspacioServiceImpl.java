package com.example.fairshareapp.service.impl;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.EspacioCreateDTO;
import com.example.fairshareapp.model.dto.EspacioResponseDTO;
import com.example.fairshareapp.model.dto.EspacioUpdateDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.service.EspacioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementacion del servicio transaccional para la administracion de espacios compartidos,
 * reglas de distribucion de gastos (50/50 vs. Proporcional) y presupuesto base.
 */
@Service
@Transactional
public class EspacioServiceImpl implements EspacioService {

    private static final String ESPACIO_NO_ENCONTRADO = "Espacio no encontrado con id: ";

    private final EspacioRepository espacioRepository;

    /**
     * Constructor para la inyeccion del repositorio de espacios.
     *
     * @param espacioRepository Repositorio de datos para espacios.
     */
    public EspacioServiceImpl(EspacioRepository espacioRepository) {
        this.espacioRepository = espacioRepository;
    }

    /**
     * Registra un nuevo espacio validando unicidad de nombre y codigo, y asignando valores por defecto.
     *
     * @param createDTO DTO con la informacion de creacion.
     * @return EspacioResponseDTO creado.
     */
    @Override
    public EspacioResponseDTO crearEspacio(EspacioCreateDTO createDTO) {
        if (createDTO.getNombre() == null || createDTO.getNombre().trim().isEmpty()) {
            throw new ReglaInvalidaException("El nombre del espacio es obligatorio");
        }

        String nombreTrimmed = createDTO.getNombre().trim();
        if (espacioRepository.existsByNombre(nombreTrimmed)) {
            throw new ReglaInvalidaException("Ya existe un espacio registrado con el nombre: " + nombreTrimmed);
        }

        String codigoFinal;
        if (createDTO.getCodigo() != null && !createDTO.getCodigo().trim().isEmpty()) {
            codigoFinal = createDTO.getCodigo().trim().toUpperCase();
            if (espacioRepository.existsByCodigo(codigoFinal)) {
                throw new ReglaInvalidaException("Ya existe un espacio registrado con el codigo: " + codigoFinal);
            }
        } else {
            codigoFinal = generarCodigoUnico(nombreTrimmed);
        }

        if (createDTO.getTipo() == null) {
            throw new ReglaInvalidaException("El tipo de espacio es obligatorio");
        }

        if (createDTO.getReglaReparto() == null) {
            throw new ReglaInvalidaException("La regla de reparto es obligatoria");
        }

        BigDecimal presupuesto = createDTO.getPresupuestoBase();
        if (presupuesto != null && presupuesto.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El presupuesto base no puede ser negativo");
        }

        Espacio espacio = Espacio.builder()
                .nombre(nombreTrimmed)
                .descripcion(createDTO.getDescripcion())
                .codigo(codigoFinal)
                .tipo(createDTO.getTipo())
                .reglaReparto(createDTO.getReglaReparto())
                .presupuestoBase(presupuesto != null ? presupuesto : BigDecimal.ZERO)
                .build();

        Espacio guardado = espacioRepository.save(espacio);
        return mapToResponseDTO(guardado);
    }

    /**
     * Obtiene el detalle de un espacio por su identificador unico.
     *
     * @param id Identificador unico del espacio.
     * @return EspacioResponseDTO correspondiente.
     */
    @Override
    @Transactional(readOnly = true)
    public EspacioResponseDTO obtenerEspacioPorId(Long id) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(ESPACIO_NO_ENCONTRADO + id));
        return mapToResponseDTO(espacio);
    }

    /**
     * Obtiene el detalle de un espacio por su codigo unico.
     *
     * @param codigo Codigo identificador del espacio.
     * @return EspacioResponseDTO correspondiente.
     */
    @Override
    @Transactional(readOnly = true)
    public EspacioResponseDTO obtenerEspacioPorCodigo(String codigo) {
        Espacio espacio = espacioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado con codigo: " + codigo));
        return mapToResponseDTO(espacio);
    }

    /**
     * Actualiza la informacion general de un espacio, permitiendo editar su regla de reparto y presupuesto.
     *
     * @param id Identificador unico del espacio a actualizar.
     * @param updateDTO Datos con las modificaciones a aplicar.
     * @return EspacioResponseDTO con los datos actualizados.
     */
    @Override
    public EspacioResponseDTO actualizarEspacio(Long id, EspacioUpdateDTO updateDTO) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(ESPACIO_NO_ENCONTRADO + id));

        if (updateDTO.getNombre() != null && !updateDTO.getNombre().trim().isEmpty()) {
            String nuevoNombre = updateDTO.getNombre().trim();
            if (espacioRepository.existsByNombreAndIdNot(nuevoNombre, id)) {
                throw new ReglaInvalidaException("Ya existe otro espacio registrado con el nombre: " + nuevoNombre);
            }
            espacio.setNombre(nuevoNombre);
        }

        if (updateDTO.getCodigo() != null && !updateDTO.getCodigo().trim().isEmpty()) {
            String nuevoCodigo = updateDTO.getCodigo().trim().toUpperCase();
            if (espacioRepository.existsByCodigoAndIdNot(nuevoCodigo, id)) {
                throw new ReglaInvalidaException("Ya existe otro espacio registrado con el codigo: " + nuevoCodigo);
            }
            espacio.setCodigo(nuevoCodigo);
        }

        if (updateDTO.getDescripcion() != null) {
            espacio.setDescripcion(updateDTO.getDescripcion());
        }

        if (updateDTO.getTipo() != null) {
            espacio.setTipo(updateDTO.getTipo());
        }

        if (updateDTO.getReglaReparto() != null) {
            espacio.setReglaReparto(updateDTO.getReglaReparto());
        }

        if (updateDTO.getPresupuestoBase() != null) {
            if (updateDTO.getPresupuestoBase().compareTo(BigDecimal.ZERO) < 0) {
                throw new ReglaInvalidaException("El presupuesto base no puede ser negativo");
            }
            espacio.setPresupuestoBase(updateDTO.getPresupuestoBase());
        }

        Espacio actualizado = espacioRepository.save(espacio);
        return mapToResponseDTO(actualizado);
    }

    /**
     * Modifica especificamente la regla de distribucion (50/50 vs. Proporcional) del espacio.
     *
     * @param id Identificador del espacio.
     * @param reglaReparto Nueva regla de distribucion a fijar.
     * @return EspacioResponseDTO con la regla modificada.
     */
    @Override
    public EspacioResponseDTO editarReglaDistribucion(Long id, ReglaReparto reglaReparto) {
        if (reglaReparto == null) {
            throw new ReglaInvalidaException("La regla de reparto no puede ser nula");
        }
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(ESPACIO_NO_ENCONTRADO + id));

        espacio.setReglaReparto(reglaReparto);
        Espacio guardado = espacioRepository.save(espacio);
        return mapToResponseDTO(guardado);
    }

    /**
     * Fija o ajusta el presupuesto base financiero asignado al espacio.
     *
     * @param id Identificador del espacio.
     * @param presupuestoBase Monto a establecer como presupuesto base.
     * @return EspacioResponseDTO con el nuevo presupuesto base.
     */
    @Override
    public EspacioResponseDTO fijarPresupuestoBase(Long id, BigDecimal presupuestoBase) {
        if (presupuestoBase == null || presupuestoBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaInvalidaException("El presupuesto base no puede ser nulo ni negativo");
        }
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(ESPACIO_NO_ENCONTRADO + id));

        espacio.setPresupuestoBase(presupuestoBase);
        Espacio guardado = espacioRepository.save(espacio);
        return mapToResponseDTO(guardado);
    }

    /**
     * Retorna la totalidad de espacios registrados en la plataforma.
     *
     * @return Lista de todos los EspacioResponseDTO.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponseDTO> listarEspacios() {
        return espacioRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Elimina el espacio indicado por su id verificando previamente su existencia.
     *
     * @param id Identificador unico del espacio.
     */
    @Override
    public void eliminarEspacio(Long id) {
        if (!espacioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(ESPACIO_NO_ENCONTRADO + id);
        }
        espacioRepository.deleteById(id);
    }

    /**
     * Mapea una entidad Espacio a su correspondiente DTO de respuesta EspacioResponseDTO.
     *
     * @param espacio Entidad Espacio a transformar.
     * @return DTO de respuesta con la informacion consolidada.
     */
    private EspacioResponseDTO mapToResponseDTO(Espacio espacio) {
        return EspacioResponseDTO.builder()
                .id(espacio.getId())
                .nombre(espacio.getNombre())
                .descripcion(espacio.getDescripcion())
                .codigo(espacio.getCodigo())
                .tipo(espacio.getTipo())
                .reglaReparto(espacio.getReglaReparto())
                .presupuestoBase(espacio.getPresupuestoBase())
                .build();
    }

    /**
     * Genera un codigo alfanumerico unico para el espacio en caso de no ser provisto.
     *
     * @param nombre Nombre del espacio utilizado como prefijo base.
     * @return Codigo unico en mayusculas.
     */
    private String generarCodigoUnico(String nombre) {
        String prefijo = nombre.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (prefijo.length() > 6) {
            prefijo = prefijo.substring(0, 6);
        }
        if (prefijo.isEmpty()) {
            prefijo = "ESP";
        }
        String sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String candidato = prefijo + "-" + sufijo;
        while (espacioRepository.existsByCodigo(candidato)) {
            sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            candidato = prefijo + "-" + sufijo;
        }
        return candidato;
    }
}
