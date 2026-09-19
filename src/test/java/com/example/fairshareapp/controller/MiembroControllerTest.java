package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.MembresiaDuplicadaException;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarSueldoDTO;
import com.example.fairshareapp.model.dto.MiembroResponseDTO;
import com.example.fairshareapp.model.dto.UnirseEspacioDTO;
import com.example.fairshareapp.model.enums.RolMiembro;
import com.example.fairshareapp.service.MiembroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador MiembroController usando MockMvc.
 * Valida los endpoints de union por espacio o por codigo directo, gestion de sueldos,
 * asignacion de roles y consulta de miembros.
 */
@ExtendWith(MockitoExtension.class)
class MiembroControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MiembroService miembroService;

    @InjectMocks
    private MiembroController miembroController;

    private MiembroResponseDTO miembroMock;

    /**
     * Configuracion inicial del entorno aislado de prueba de MockMvc.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(miembroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        miembroMock = MiembroResponseDTO.builder()
                .id(1L)
                .espacioId(10L)
                .usuarioId(100L)
                .nombreUsuario("uriel")
                .emailUsuario("uriel@example.com")
                .rol(RolMiembro.ADMIN)
                .sueldoDeclarado(BigDecimal.valueOf(350000.00))
                .build();
    }

    /**
     * Valida la union exitosa a un espacio por su id retornando codigo HTTP 201 Created.
     */
    @Test
    void unirseAEspacio_DatosValidos_RetornaCreated() throws Exception {
        when(miembroService.unirseAEspacio(eq(10L), any(UnirseEspacioDTO.class))).thenReturn(miembroMock);

        String jsonPayload = """
                {
                    "codigo": "DEPTO-BEL",
                    "usuarioId": 100,
                    "sueldoDeclarado": 350000.00
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/10/unirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.espacioId").value(10L))
                .andExpect(jsonPath("$.usuarioId").value(100L))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    /**
     * Valida que al intentar unirse a un espacio del cual ya es miembro se retorne codigo HTTP 409 Conflict.
     */
    @Test
    void unirseAEspacio_MembresiaDuplicada_RetornaConflict() throws Exception {
        when(miembroService.unirseAEspacio(eq(10L), any(UnirseEspacioDTO.class)))
                .thenThrow(new MembresiaDuplicadaException("El usuario con id 100 ya es miembro del espacio con id 10"));

        String jsonPayload = """
                {
                    "codigo": "DEPTO-BEL",
                    "usuarioId": 100
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/10/unirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict());
    }

    /**
     * Valida la union directa a un espacio a partir de su codigo de invitacion retornando codigo HTTP 201 Created.
     */
    @Test
    void unirsePorCodigo_DatosValidos_RetornaCreated() throws Exception {
        when(miembroService.unirsePorCodigo(any(UnirseEspacioDTO.class))).thenReturn(miembroMock);

        String jsonPayload = """
                {
                    "codigo": "DEPTO-BEL",
                    "usuarioId": 100
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/unirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    /**
     * Valida la obtencion de miembros de un espacio retornando codigo HTTP 200 OK.
     */
    @Test
    void listarMiembros_EspacioExistente_RetornaOk() throws Exception {
        when(miembroService.listarMiembros(10L)).thenReturn(List.of(miembroMock));

        mockMvc.perform(get("/api/v1/espacios/10/miembros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].usuarioId").value(100L));
    }

    /**
     * Valida que listar miembros de un espacio inexistente retorne codigo HTTP 404 Not Found.
     */
    @Test
    void listarMiembros_EspacioInexistente_RetornaNotFound() throws Exception {
        when(miembroService.listarMiembros(99L))
                .thenThrow(new RecursoNoEncontradoException("Espacio no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/espacios/99/miembros"))
                .andExpect(status().isNotFound());
    }

    /**
     * Valida la actualizacion del sueldo declarado retornando codigo HTTP 200 OK.
     */
    @Test
    void actualizarSueldo_DatosValidos_RetornaOk() throws Exception {
        MiembroResponseDTO miembroActualizado = MiembroResponseDTO.builder()
                .id(1L)
                .espacioId(10L)
                .usuarioId(100L)
                .rol(RolMiembro.ADMIN)
                .sueldoDeclarado(BigDecimal.valueOf(500000.00))
                .build();

        when(miembroService.actualizarSueldo(eq(10L), eq(100L), any(ActualizarSueldoDTO.class)))
                .thenReturn(miembroActualizado);

        String jsonPayload = """
                {
                    "sueldoDeclarado": 500000.00
                }
                """;

        mockMvc.perform(put("/api/v1/espacios/10/miembros/100/sueldo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sueldoDeclarado").value(500000.00));
    }

    /**
     * Valida la asignacion de un rol sin solicitante especificado retornando codigo HTTP 200 OK.
     */
    @Test
    void asignarRol_SinSolicitante_RetornaOk() throws Exception {
        MiembroResponseDTO miembroRolCambiado = MiembroResponseDTO.builder()
                .id(1L)
                .rol(RolMiembro.ADMIN)
                .build();

        when(miembroService.asignarRol(10L, 100L, RolMiembro.ADMIN)).thenReturn(miembroRolCambiado);

        mockMvc.perform(patch("/api/v1/espacios/10/miembros/100/rol")
                        .param("rol", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        verify(miembroService).asignarRol(10L, 100L, RolMiembro.ADMIN);
    }

    /**
     * Valida la asignacion de un rol con solicitante administrador retornando codigo HTTP 200 OK.
     */
    @Test
    void asignarRol_ConSolicitante_RetornaOk() throws Exception {
        MiembroResponseDTO miembroRolCambiado = MiembroResponseDTO.builder()
                .id(1L)
                .rol(RolMiembro.ADMIN)
                .build();

        when(miembroService.asignarRol(10L, 100L, RolMiembro.ADMIN, 50L)).thenReturn(miembroRolCambiado);

        mockMvc.perform(patch("/api/v1/espacios/10/miembros/100/rol")
                        .param("rol", "ADMIN")
                        .param("solicitanteId", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        verify(miembroService).asignarRol(10L, 100L, RolMiembro.ADMIN, 50L);
    }

    /**
     * Valida que si el solicitante no es administrador se retorne codigo HTTP 400 Bad Request.
     */
    @Test
    void asignarRol_SolicitanteNoAdmin_RetornaBadRequest() throws Exception {
        when(miembroService.asignarRol(10L, 100L, RolMiembro.ADMIN, 50L))
                .thenThrow(new ReglaInvalidaException("Acceso denegado: se requieren permisos de administrador en el espacio"));

        mockMvc.perform(patch("/api/v1/espacios/10/miembros/100/rol")
                        .param("rol", "ADMIN")
                        .param("solicitanteId", "50"))
                .andExpect(status().isBadRequest());
    }
}
