package com.example.fairshareapp.service;

import com.example.fairshareapp.model.dto.EspacioCreateDTO;
import com.example.fairshareapp.model.dto.EspacioResponseDTO;
import com.example.fairshareapp.model.dto.EspacioUpdateDTO;
import com.example.fairshareapp.model.enums.ReglaReparto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz de servicio que define los casos de uso para la administracion de espacios compartidos,
 * incluyendo creacion, configuracion de reglas de reparto y fijacion de presupuesto base.
 */
public interface EspacioService {

    /**
     * Crea y persiste un nuevo espacio compartido validando nombres y codigos unicos.
     *
     * @param createDTO Datos para la creacion del espacio.
     * @return EspacioResponseDTO con el detalle del espacio registrado.
     */
    EspacioResponseDTO crearEspacio(EspacioCreateDTO createDTO);

    /**
     * Obtiene los datos de un espacio por su identificador unico.
     *
     * @param id Identificador unico del espacio.
     * @return EspacioResponseDTO del espacio solicitado.
     */
    EspacioResponseDTO obtenerEspacioPorId(Long id);

    /**
     * Busca un espacio por su codigo unico.
     *
     * @param codigo Codigo identificador del espacio.
     * @return EspacioResponseDTO del espacio solicitado.
     */
    EspacioResponseDTO obtenerEspacioPorCodigo(String codigo);

    /**
     * Actualiza los datos de un espacio existente.
     *
     * @param id Identificador unico del espacio a actualizar.
     * @param updateDTO Informacion con los cambios solicitados.
     * @return EspacioResponseDTO actualizado.
     */
    EspacioResponseDTO actualizarEspacio(Long id, EspacioUpdateDTO updateDTO);

    /**
     * Edita la regla de reparto o distribucion de gastos del espacio (50/50 vs. Proporcional).
     *
     * @param id Identificador unico del espacio.
     * @param reglaReparto Nueva regla de distribucion seleccionada.
     * @return EspacioResponseDTO con la regla actualizada.
     */
    EspacioResponseDTO editarReglaDistribucion(Long id, ReglaReparto reglaReparto);

    /**
     * Fija o actualiza el presupuesto base financiero del espacio compartido.
     *
     * @param id Identificador unico del espacio.
     * @param presupuestoBase Nuevo monto de presupuesto base.
     * @return EspacioResponseDTO con el presupuesto fijado.
     */
    EspacioResponseDTO fijarPresupuestoBase(Long id, BigDecimal presupuestoBase);

    /**
     * Retorna la lista con todos los espacios registrados en la plataforma.
     *
     * @return Lista de EspacioResponseDTO.
     */
    List<EspacioResponseDTO> listarEspacios();

    /**
     * Elimina un espacio por su identificador unico.
     *
     * @param id Identificador unico del espacio a eliminar.
     */
    void eliminarEspacio(Long id);
}
