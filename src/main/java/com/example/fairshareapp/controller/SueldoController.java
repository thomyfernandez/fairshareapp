package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.model.response.SueldoUpsertResult;
import com.example.fairshareapp.service.SueldoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sueldos")
public class SueldoController {

    private final SueldoService sueldoService;

    /**
     * Constructor con inyeccion de dependencias del servicio de sueldos.
     *
     * @param sueldoService Servicio para la gestion de sueldos.
     */
    public SueldoController(SueldoService sueldoService) {
        this.sueldoService = sueldoService;
    }

    /**
     * Retorna el listado de sueldos registrados, filtrando opcionalmente por usuario.
     *
     * @param usuarioId Identificador opcional del usuario para filtrar sueldos.
     * @return ResponseEntity con la lista de SueldoResponse.
     */
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("#usuarioId == null or @acceso.propio(#usuarioId)")
    public ResponseEntity<List<SueldoResponse>> getAllSueldos(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId == null && usuarioAutenticado() != null) usuarioId = usuarioAutenticado().getId();
        List<SueldoResponse> sueldos = usuarioId != null
                ? sueldoService.obtenerSueldosPorUsuario(usuarioId)
                : sueldoService.obtenerTodos();
        return ResponseEntity.ok(sueldos);
    }

    /**
     * Retorna el detalle de un sueldo por su identificador.
     *
     * @param id Identificador unico del sueldo.
     * @return ResponseEntity con el SueldoResponse encontrado.
     */
    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("@acceso.sueldo(#id)")
    public ResponseEntity<SueldoResponse> getSueldo(@PathVariable Long id) {
        return ResponseEntity.ok(sueldoService.obtenerSueldo(id));
    }

    /**
     * Registra o actualiza (upsert) el sueldo de un usuario para un periodo mensual especifico.
     * Si no se envia usuarioId en el body, se utiliza la identidad autenticada del solicitante.
     * Devuelve 201 Created si crea un registro nuevo, o 200 OK si actualiza uno existente.
     *
     * @param request Datos del sueldo incluyendo monto, tipo, frecuencia, mes y anio.
     * @return ResponseEntity con el SueldoResponse creado o actualizado.
     */
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("#request.usuarioId == null or @acceso.propio(#request.usuarioId)")
    public ResponseEntity<SueldoResponse> crearSueldo(@Valid @RequestBody SueldoRequest request) {
        Long usuarioId = resolverUsuarioId(request.getUsuarioId());
        SueldoUpsertResult resultado = sueldoService.crearOActualizarSueldo(usuarioId, request);
        HttpStatus status = resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.sueldo());
    }

    /**
     * Actualiza los datos de un sueldo existente identificado por su ID. El usuario propietario
     * no puede reasignarse mediante este endpoint.
     *
     * @param id Identificador unico del sueldo a modificar.
     * @param request Nuevos datos del sueldo.
     * @return ResponseEntity con el SueldoResponse actualizado.
     */
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("@acceso.sueldo(#id)")
    public ResponseEntity<SueldoResponse> updateSueldo(@PathVariable Long id, @Valid @RequestBody SueldoRequest request) {
        validarAccesoSueldo(id);
        return ResponseEntity.ok(sueldoService.actualizarSueldo(id, request));
    }

    /**
     * Elimina un registro de sueldo segun su identificador unico.
     *
     * @param id Identificador unico del sueldo a borrar.
     * @return ResponseEntity vacio con codigo HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("@acceso.sueldo(#id)")
    public ResponseEntity<Void> deleteSueldo(@PathVariable Long id) {
        validarAccesoSueldo(id);
        sueldoService.eliminarSueldo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resuelve el identificador de usuario a utilizar para crear un sueldo: exige que se
     * envie un usuarioId valido o que exista una identidad autenticada. Rechaza el intento
     * de registrar el sueldo de un usuario distinto al autenticado.
     *
     * @param usuarioIdSolicitado Identificador de usuario enviado en el request, si lo hubiera.
     * @return Identificador de usuario a utilizar.
     */
    private Long resolverUsuarioId(Long usuarioIdSolicitado) {
        Usuario autenticado = usuarioAutenticado();

        if (usuarioIdSolicitado != null) {
            if (autenticado != null && !usuarioIdSolicitado.equals(autenticado.getId())) {
                throw new com.example.fairshareapp.exception.AccesoDenegadoException("Acceso denegado: no puede registrar el sueldo de otro usuario");
            }
            return usuarioIdSolicitado;
        }

        if (autenticado != null) {
            return autenticado.getId();
        }

        throw new ReglaInvalidaException("Debe especificarse un usuarioId valido o autenticarse");
    }

    /**
     * Valida que, de existir una identidad autenticada, el sueldo consultado le pertenezca.
     *
     * @param sueldoId Identificador del sueldo sobre el que se quiere operar.
     */
    private void validarAccesoSueldo(Long sueldoId) {
        Usuario autenticado = usuarioAutenticado();
        if (autenticado == null) {
            return;
        }
        SueldoResponse existente = sueldoService.obtenerSueldo(sueldoId);
        if (existente.getUsuarioId() != null && !existente.getUsuarioId().equals(autenticado.getId())) {
            throw new com.example.fairshareapp.exception.AccesoDenegadoException("Acceso denegado: el sueldo pertenece a otro usuario");
        }
    }

    private Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        return null;
    }
}
