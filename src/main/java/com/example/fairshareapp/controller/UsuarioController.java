package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.request.LoginRequest;
import com.example.fairshareapp.model.request.RegistroUsuarioRequest;
import com.example.fairshareapp.model.response.LoginResponse;
import com.example.fairshareapp.model.response.UsuarioResponse;
import com.example.fairshareapp.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la administracion y autenticacion de usuarios.
 * Expone operaciones para registrar nuevos usuarios, validar credenciales de acceso
 * y consultar el listado completo de usuarios registrados.
 */
@RestController
@RequestMapping({"/api/v1/usuarios", "/api/usuario"})
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Constructor con inyeccion de dependencias del servicio de usuarios.
     *
     * @param usuarioService Servicio para la logica de negocio y autenticacion de usuarios.
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Registra un nuevo usuario en la base de datos del sistema.
     *
     * @param request Datos requeridos para el alta del usuario.
     * @return ResponseEntity con mensaje de confirmacion y codigo HTTP 201 Created.
     */
    @PostMapping("/registro")
    public ResponseEntity<String> registro(@Valid @RequestBody RegistroUsuarioRequest request) {
        usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }

    /**
     * Valida las credenciales de acceso y autentica a un usuario, generando su JWT de acceso.
     *
     * @param request Credenciales de acceso compuestas por email y contrasena.
     * @return ResponseEntity con el token JWT y los datos del usuario, codigo HTTP 200 OK.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = usuarioService.loginUsuario(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna el listado completo de todos los usuarios registrados en el sistema.
     * Soporta la ruta canonica y la ruta heredada con sufijo /get.
     *
     * @return ResponseEntity con la lista de usuarios y codigo HTTP 200 OK.
     */
    @GetMapping({"", "/get"})
    public ResponseEntity<List<UsuarioResponse>> getAllUsuarios() {
        List<UsuarioResponse> res = usuarioService.getAllUsuarios();
        return ResponseEntity.ok(res);
    }
}
