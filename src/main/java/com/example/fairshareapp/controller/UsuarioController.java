package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.request.LoginRequest;
import com.example.fairshareapp.model.request.RegistroUsuarioRequest;
import com.example.fairshareapp.model.response.LoginResponse;
import com.example.fairshareapp.model.response.UsuarioResponse;
import com.example.fairshareapp.security.AuthCookieService;
import com.example.fairshareapp.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la administracion y autenticacion de usuarios.
 * Expone operaciones para registrar nuevos usuarios, validar credenciales de acceso
 * y consultar el listado completo de usuarios registrados.
 */
@RestController
@RequestMapping({"/api/v1/usuarios", "/api/usuario"})
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthCookieService authCookieService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param usuarioService    Servicio para la logica de negocio y autenticacion de usuarios.
     * @param authCookieService Fabrica de la cookie HttpOnly que transporta el JWT.
     */
    public UsuarioController(UsuarioService usuarioService, AuthCookieService authCookieService) {
        this.usuarioService = usuarioService;
        this.authCookieService = authCookieService;
    }

    /**
     * Registra un nuevo usuario en la base de datos del sistema.
     *
     * @param request Datos requeridos para el alta del usuario.
     * @return ResponseEntity con mensaje de confirmacion y codigo HTTP 201 Created.
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarUsuario(request));
    }

    /**
     * Valida las credenciales de acceso y autentica a un usuario.
     * El JWT se entrega en una cookie HttpOnly (Set-Cookie) y no en el body, para que JavaScript
     * (y un eventual script inyectado por XSS) no pueda leerlo. Clientes de API (Postman) pueden
     * tomarlo de la cookie y enviarlo como "Authorization: Bearer".
     *
     * @param request Credenciales de acceso compuestas por email y contrasena.
     * @return ResponseEntity con los datos del usuario (sin token) y la cookie de sesion, codigo HTTP 200 OK.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = usuarioService.loginUsuario(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.crear(response.token()).toString())
                .body(response.sinToken());
    }

    /**
     * Cierra la sesion pidiendo al navegador que borre la cookie (Max-Age=0); JavaScript no puede
     * borrar una cookie HttpOnly.
     *
     * @return 204 No Content con la cookie vencida.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.borrar().toString())
                .build();
    }

    /**
     * Entrega el token CSRF. Al resolverlo, Spring Security escribe la cookie XSRF-TOKEN, que el
     * frontend repite en el header X-XSRF-TOKEN de cada solicitud que modifica datos.
     *
     * @param csrfToken Token CSRF de la solicitud actual.
     * @return 200 con el nombre del header y el token.
     */
    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of("headerName", csrfToken.getHeaderName(), "token", csrfToken.getToken()));
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

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me() {
        return ResponseEntity.ok(UsuarioResponse.de(com.example.fairshareapp.security.UsuarioActual.requerido()));
    }
}
