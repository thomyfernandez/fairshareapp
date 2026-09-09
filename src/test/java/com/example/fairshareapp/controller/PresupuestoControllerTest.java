package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.dto.PresupuestoResponseDTO;
import com.example.fairshareapp.service.PresupuestoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador PresupuestoController usando MockMvc.
 * Valida el enrutamiento y codigos de estado HTTP de los endpoints REST expuestos.
 */
@ExtendWith(MockitoExtension.class)
class PresupuestoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PresupuestoService presupuestoService;

    @InjectMocks
    private PresupuestoController presupuestoController;

    /**
     * Inicializa el entorno de prueba aislado de MockMvc con el manejador global de excepciones.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(presupuestoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Prueba el endpoint de fijar presupuesto retornando codigo 200 OK.
     */
    @Test
    void fijarPresupuesto_payloadValido_retornaOk() throws Exception {
        PresupuestoResponseDTO detalle = PresupuestoResponseDTO.builder()
                .id(5L).espacioId(1L).usuarioId(2L).usuarioNombre("Maria Gomez")
                .anio(2026).mes(9).montoLimite(BigDecimal.valueOf(50000)).build();

        when(presupuestoService.fijarPresupuesto(eq(1L), eq(2L), eq(2026), eq(9), any())).thenReturn(detalle);

        String jsonBody = """
                {"montoLimite": 50000}
                """;

        mockMvc.perform(put("/api/v1/espacios/1/presupuestos/usuarios/2?anio=2026&mes=9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.montoLimite").value(50000));
    }

    /**
     * Prueba el endpoint de fijar presupuesto para un usuario inexistente retornando codigo 404 Not Found.
     */
    @Test
    void fijarPresupuesto_usuarioInexistente_retornaNotFound() throws Exception {
        when(presupuestoService.fijarPresupuesto(eq(1L), eq(99L), eq(2026), eq(9), any()))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado con id: 99"));

        String jsonBody = """
                {"montoLimite": 50000}
                """;

        mockMvc.perform(put("/api/v1/espacios/1/presupuestos/usuarios/99?anio=2026&mes=9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound());
    }

    /**
     * Prueba el endpoint de listado de presupuestos retornando codigo 200 OK.
     */
    @Test
    void listarPresupuestos_espacioValido_retornaOkConLista() throws Exception {
        PresupuestoResponseDTO detalle = PresupuestoResponseDTO.builder()
                .id(5L).espacioId(1L).usuarioId(2L).usuarioNombre("Maria Gomez")
                .anio(2026).mes(9).montoLimite(BigDecimal.valueOf(50000)).build();

        when(presupuestoService.listarPresupuestos(1L, null, null)).thenReturn(List.of(detalle));

        mockMvc.perform(get("/api/v1/espacios/1/presupuestos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L));
    }
}
