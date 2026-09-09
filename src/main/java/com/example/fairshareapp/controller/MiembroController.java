package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.ActualizarSueldoDTO;
import com.example.fairshareapp.model.dto.MiembroResponseDTO;
import com.example.fairshareapp.model.dto.UnirseEspacioDTO;
import com.example.fairshareapp.model.enums.RolMiembro;
import com.example.fairshareapp.service.MiembroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la administracion de miembros de un espacio compartido.
 * Expone operaciones para unirse mediante codigo de invitacion, consultar miembros,
 * asignar roles y gestionar el sueldo mensual declarado.
 */
@RestController
@RequestMapping("/api/v1/espacios/{id}")
public class MiembroController {

    private final MiembroService miembroService;

    /**
     * Constructor con inyeccion de dependencia del servicio de miembros.
     *
     * @param miembroService Servicio de miembros de espacios compartidos.
     */
    public MiembroController(MiembroService miembroService) {
        this.miembroService = miembroService;
    }

    /**
     * Une a un usuario a un espacio compartido validando el codigo de invitacion.
     *
     * @param id Identificador unico del espacio al que se desea unir.
     * @param unirseDTO Datos de union, incluyendo codigo, usuario y sueldo declarado opcional.
     * @return ResponseEntity con el MiembroResponseDTO creado y codigo HTTP 201 Created.
     */
    @PostMapping("/unirse")
    public ResponseEntity<MiembroResponseDTO> unirseAEspacio(@PathVariable Long id,
                                                              @Valid @RequestBody UnirseEspacioDTO unirseDTO) {
        MiembroResponseDTO response = miembroService.unirseAEspacio(id, unirseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene el listado de miembros pertenecientes a un espacio.
     *
     * @param id Identificador unico del espacio.
     * @return ResponseEntity con la lista de MiembroResponseDTO y codigo HTTP 200 OK.
     */
    @GetMapping("/miembros")
    public ResponseEntity<List<MiembroResponseDTO>> listarMiembros(@PathVariable Long id) {
        List<MiembroResponseDTO> response = miembroService.listarMiembros(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra o actualiza el sueldo mensual declarado de un miembro dentro de un espacio.
     *
     * @param id Identificador unico del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param actualizarSueldoDTO Datos con el nuevo sueldo declarado.
     * @return ResponseEntity con el MiembroResponseDTO actualizado y codigo HTTP 200 OK.
     */
    @PutMapping("/miembros/{usuarioId}/sueldo")
    public ResponseEntity<MiembroResponseDTO> actualizarSueldo(@PathVariable Long id,
                                                                @PathVariable Long usuarioId,
                                                                @Valid @RequestBody ActualizarSueldoDTO actualizarSueldoDTO) {
        MiembroResponseDTO response = miembroService.actualizarSueldo(id, usuarioId, actualizarSueldoDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Asigna o modifica el rol de un miembro dentro de un espacio.
     *
     * @param id Identificador unico del espacio.
     * @param usuarioId Identificador del usuario miembro.
     * @param rol Nuevo rol a asignar.
     * @return ResponseEntity con el MiembroResponseDTO con el rol actualizado.
     */
    @PatchMapping("/miembros/{usuarioId}/rol")
    public ResponseEntity<MiembroResponseDTO> asignarRol(@PathVariable Long id,
                                                          @PathVariable Long usuarioId,
                                                          @RequestParam RolMiembro rol) {
        MiembroResponseDTO response = miembroService.asignarRol(id, usuarioId, rol);
        return ResponseEntity.ok(response);
    }
}
