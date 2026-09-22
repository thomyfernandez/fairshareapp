package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.model.response.SueldoUpsertResult;
import com.example.fairshareapp.service.SueldoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador SueldoController usando MockMvc.
 * Valida codigos de respuesta HTTP, ausencia de usuario por defecto oculto,
 * duplicados por periodo y control de accesos.
 */
@ExtendWith(MockitoExtension.class)
class SueldoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SueldoService sueldoService;

    @InjectMocks
    private SueldoController sueldoController;

    private SueldoResponse sueldoResponse;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders.standaloneSetup(sueldoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sueldoResponse = new SueldoResponse();
        sueldoResponse.setId(10L);
        sueldoResponse.setUsuarioId(1L);
        sueldoResponse.setMonto(BigDecimal.valueOf(850000.00));
        sueldoResponse.setTipo(TipoSueldo.FIJO);
        sueldoResponse.setFrecuencia(FrecuenciaSueldo.MENSUAL);
        sueldoResponse.setMes(9);
        sueldoResponse.setAnio(2026);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Long usuarioId) {
        Usuario usuario = Usuario.builder().id(usuarioId).nombre("Autenticado").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    /**
     * Valida que crear un sueldo para un periodo nuevo retorne HTTP 201 Created.
     */
    @Test
    void crearSueldo_PeriodoNuevo_RetornaCreated() throws Exception {
        when(sueldoService.crearOActualizarSueldo(eq(1L), any(SueldoRequest.class)))
                .thenReturn(new SueldoUpsertResult(sueldoResponse, true));

        String payload = """
                {
                    "usuarioId": 1,
                    "monto": 850000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL",
                    "mes": 9,
                    "anio": 2026
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    /**
     * Valida que actualizar el mismo periodo mediante POST (upsert) retorne HTTP 200 OK
     * en lugar de crear un registro duplicado.
     */
    @Test
    void crearSueldo_MismoPeriodoExistente_RetornaOk() throws Exception {
        when(sueldoService.crearOActualizarSueldo(eq(1L), any(SueldoRequest.class)))
                .thenReturn(new SueldoUpsertResult(sueldoResponse, false));

        String payload = """
                {
                    "usuarioId": 1,
                    "monto": 850000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL",
                    "mes": 9,
                    "anio": 2026
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    /**
     * Valida que se rechace la creacion de un sueldo sin usuarioId y sin identidad
     * autenticada, evitando el uso silencioso de un usuario por defecto.
     */
    @Test
    void crearSueldo_SinUsuarioNiAutenticacion_RetornaBadRequest() throws Exception {
        String payload = """
                {
                    "monto": 850000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL"
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(sueldoService, never()).crearOActualizarSueldo(any(), any());
    }

    /**
     * Valida que la identidad autenticada se utilice como usuarioId cuando no se envia en el body.
     */
    @Test
    void crearSueldo_SinUsuarioIdConAutenticacion_UsaIdentidadAutenticada() throws Exception {
        autenticarComo(1L);
        when(sueldoService.crearOActualizarSueldo(eq(1L), any(SueldoRequest.class)))
                .thenReturn(new SueldoUpsertResult(sueldoResponse, true));

        String payload = """
                {
                    "monto": 850000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL"
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        verify(sueldoService).crearOActualizarSueldo(eq(1L), any(SueldoRequest.class));
    }

    /**
     * Valida que se rechace un monto negativo con HTTP 400 Bad Request.
     */
    @Test
    void crearSueldo_MontoNegativo_RetornaBadRequest() throws Exception {
        String payload = """
                {
                    "usuarioId": 1,
                    "monto": -500.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL"
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(sueldoService, never()).crearOActualizarSueldo(any(), any());
    }

    /**
     * Valida que se rechace un mes fuera del rango 1-12 con HTTP 400 Bad Request.
     */
    @Test
    void crearSueldo_MesInvalido_RetornaBadRequest() throws Exception {
        when(sueldoService.crearOActualizarSueldo(eq(1L), any(SueldoRequest.class)))
                .thenThrow(new ReglaInvalidaException("El mes debe estar entre 1 y 12"));

        String payload = """
                {
                    "usuarioId": 1,
                    "monto": 850000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL",
                    "mes": 13,
                    "anio": 2026
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    /**
     * Valida que crear un sueldo para un usuario inexistente retorne HTTP 404 Not Found.
     */
    @Test
    void crearSueldo_UsuarioInexistente_RetornaNotFound() throws Exception {
        when(sueldoService.crearOActualizarSueldo(eq(99L), any(SueldoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado con id: 99"));

        String payload = """
                {
                    "usuarioId": 99,
                    "monto": 850000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL"
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    /**
     * Valida que un usuario autenticado no pueda registrar el sueldo de otro usuario.
     */
    @Test
    void crearSueldo_UsuarioIdDistintoAlAutenticado_RetornaForbidden() throws Exception {
        autenticarComo(5L);

        String payload = """
                {
                    "usuarioId": 1,
                    "monto": 850000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL"
                }
                """;

        mockMvc.perform(post("/api/v1/sueldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        verify(sueldoService, never()).crearOActualizarSueldo(any(), any());
    }

    /**
     * Valida que la consulta por usuario retorne unicamente los sueldos de ese usuario.
     */
    @Test
    void getAllSueldos_ConUsuarioId_RetornaSoloDelUsuario() throws Exception {
        when(sueldoService.obtenerSueldosPorUsuario(1L)).thenReturn(List.of(sueldoResponse));

        mockMvc.perform(get("/api/v1/sueldos").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].usuarioId").value(1L));

        verify(sueldoService, never()).obtenerTodos();
    }

    /**
     * Valida la actualizacion de un sueldo existente retornando HTTP 200 OK.
     */
    @Test
    void updateSueldo_DatosValidos_RetornaOk() throws Exception {
        when(sueldoService.actualizarSueldo(eq(10L), any(SueldoRequest.class))).thenReturn(sueldoResponse);

        String payload = """
                {
                    "monto": 900000.00,
                    "tipo": "VARIABLE",
                    "frecuencia": "QUINCENAL"
                }
                """;

        mockMvc.perform(put("/api/v1/sueldos/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    /**
     * Valida que actualizar un sueldo inexistente retorne HTTP 404 Not Found.
     */
    @Test
    void updateSueldo_Inexistente_RetornaNotFound() throws Exception {
        when(sueldoService.actualizarSueldo(eq(999L), any(SueldoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("Sueldo no encontrado con id: 999"));

        String payload = """
                {
                    "monto": 900000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL"
                }
                """;

        mockMvc.perform(put("/api/v1/sueldos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    /**
     * Valida que un usuario autenticado no pueda actualizar el sueldo de otro usuario.
     */
    @Test
    void updateSueldo_AccesoNoAutorizado_RetornaForbidden() throws Exception {
        autenticarComo(5L);
        when(sueldoService.obtenerSueldo(10L)).thenReturn(sueldoResponse);

        String payload = """
                {
                    "monto": 900000.00,
                    "tipo": "FIJO",
                    "frecuencia": "MENSUAL"
                }
                """;

        mockMvc.perform(put("/api/v1/sueldos/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        verify(sueldoService, never()).actualizarSueldo(anyLong(), any());
    }

    /**
     * Valida la eliminacion de un sueldo existente retornando HTTP 204 No Content y cuerpo vacio.
     */
    @Test
    void deleteSueldo_Existente_RetornaNoContentSinCuerpo() throws Exception {
        mockMvc.perform(delete("/api/v1/sueldos/10"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(sueldoService).eliminarSueldo(10L);
    }

    /**
     * Valida que eliminar un sueldo inexistente retorne HTTP 404 Not Found.
     */
    @Test
    void deleteSueldo_Inexistente_RetornaNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RecursoNoEncontradoException("Sueldo no encontrado con id: 999"))
                .when(sueldoService).eliminarSueldo(999L);

        mockMvc.perform(delete("/api/v1/sueldos/999"))
                .andExpect(status().isNotFound());
    }
}
