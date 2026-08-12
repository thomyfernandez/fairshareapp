package com.example.fairshareapp.service;

import com.example.fairshareapp.model.Gasto;
import com.example.fairshareapp.repository.GastoRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestion de los gastos de la aplicacion.
 * Maneja la logica entre el controlador REST y el repositorio de datos.
 */
@Service
@Transactional
public class GastoService {

    private final GastoRepository gastoRepository;

    /**
     * Constructor para la inyeccion de dependencias del repositorio de gastos.
     * 
     * @param gastoRepository Instancia del repositorio de datos de gastos.
     */
    @Autowired
    public GastoService(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    /**
     * Obtiene la lista completa de todos los gastos registrados.
     * 
     * @return Lista de objetos Gasto.
     */
    public List<Gasto> obtenerTodos() {
        return gastoRepository.findAll();
    }

    /**
     * Busca un gasto especifico por su identificador unico.
     * 
     * @param id Identificador unico del gasto.
     * @return Optional con el objeto Gasto encontrado, o vacio si no existe.
     */
    public Optional<Gasto> obtenerPorId(Long id) {
        return gastoRepository.findById(id);
    }

    /**
     * Guarda un nuevo gasto o actualiza uno existente en la base de datos.
     * 
     * @param gasto Instancia del gasto a registrar o actualizar.
     * @return Objeto Gasto persistido en la base de datos.
     */
    public Gasto guardar(Gasto gasto) {
        return gastoRepository.save(gasto);
    }

    /**
     * Elimina un gasto existente de la base de datos a partir de su ID.
     * 
     * @param id Identificador unico del gasto a eliminar.
     */
    public void eliminarPorId(Long id) {
        gastoRepository.deleteById(id);
    }
}