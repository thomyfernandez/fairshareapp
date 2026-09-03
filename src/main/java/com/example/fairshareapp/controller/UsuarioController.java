package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.Usuario;
import com.example.fairshareapp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gestionar la API de usuarios de la aplicacion FairShare.
 * Expone los endpoints HTTP para el registro y consulta de usuarios.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Endpoint HTTP GET para listar todos los usuarios registrados.
     *
     * @return Lista de usuarios con codigo HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    /**
     * Endpoint HTTP GET para buscar un usuario por su identificador unico.
     *
     * @param id Identificador unico del usuario a consultar.
     * @return Usuario encontrado con HTTP 200 OK o HTTP 404 Not Found si no existe
     *         (lanzado como ResourceNotFoundException y traducido por el manejador global).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorIdOLanzar(id));
    }

    /**
     * Endpoint HTTP POST para registrar un nuevo usuario en el sistema.
     *
     * @param usuario Objeto Usuario recibido en el cuerpo de la peticion HTTP
     *                (nombre, email, fechaNacimiento y sexo).
     * @return Usuario registrado con codigo HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioRegistrado = usuarioService.registrar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRegistrado);
    }
}
