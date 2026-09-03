package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.ArgumentInvalidException;
import com.example.fairshareapp.exception.ResourceNotFoundException;
import com.example.fairshareapp.model.Usuario;
import com.example.fairshareapp.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestion de los usuarios de la aplicacion.
 * Maneja la logica entre el controlador REST y el repositorio de datos.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene la lista completa de todos los usuarios registrados.
     *
     * @return Lista de objetos Usuario.
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario especifico por su identificador unico.
     *
     * @param id Identificador unico del usuario.
     * @return Optional con el objeto Usuario encontrado, o vacio si no existe.
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su ID o lanza una excepcion si no existe.
     *
     * @param id Identificador unico del usuario.
     * @return Objeto Usuario encontrado.
     * @throws ResourceNotFoundException si no existe un usuario con el ID indicado.
     */
    public Usuario obtenerPorIdOLanzar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Registra un nuevo usuario en la base de datos, validando sus datos basicos.
     *
     * @param usuario Instancia del usuario a registrar.
     * @return Objeto Usuario persistido en la base de datos, con su ID generado.
     * @throws ArgumentInvalidException si algun dato obligatorio es invalido o el email ya esta registrado.
     */
    public Usuario registrar(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new ArgumentInvalidException("El nombre del usuario es obligatorio.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new ArgumentInvalidException("El email del usuario es obligatorio.");
        }
        if (usuario.getFechaNacimiento() == null || usuario.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new ArgumentInvalidException("La fecha de nacimiento es obligatoria y no puede ser futura.");
        }
        if (usuario.getSexo() == null) {
            throw new ArgumentInvalidException("El sexo del usuario es obligatorio.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new ArgumentInvalidException("Ya existe un usuario registrado con el email: " + usuario.getEmail());
        }
        return usuarioRepository.save(usuario);
    }
}
