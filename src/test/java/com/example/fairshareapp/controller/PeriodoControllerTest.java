package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.PeriodoResponseDTO;
import com.example.fairshareapp.model.enums.EstadoPeriodo;
import com.example.fairshareapp.service.PeriodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador PeriodoController usando MockMvc.
 * Valida el enrutamiento y codigos de estado HTTP de los endpoints REST expuestos.
 */
@ExtendWith(MockitoExtension.class)
class PeriodoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PeriodoService periodoService;

    @InjectMocks
    private PeriodoController periodoController;

    /**
     * Inicializa el entorno de prueba aislado de MockMvc con el manejador global de excepciones.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(periodoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Prueba el endpoint de apertura de periodo retornando codigo 201 Created.
     */
    @Test
    void abrirPeriodo_payloadValido_retornaCreated() throws Exception {
        PeriodoResponseDTO detalle = PeriodoResponseDTO.builder()
                .id(10L).espacioId(1L).anio(2026).mes(9).estado(EstadoPeriodo.ABIERTO).build();

        when(periodoService.abrirPeriodo(eq(1L), any())).thenReturn(detalle);

        String jsonBody = """
                {"anio": 2026, "mes": 9}
                """;

        mockMvc.perform(post("/api/v1/espacios/1/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.estado").value("ABIERTO"));
    }

    /**
     * Prueba el endpoint de apertura de periodo retornando codigo 400 Bad Request cuando ya existe uno cargado.
     */
    @Test
    void abrirPeriodo_yaExiste_retornaBadRequest() throws Exception {
        when(periodoService.abrirPeriodo(eq(1L), any()))
                .thenThrow(new ReglaInvalidaException("Ya existe un periodo cargado para 9/2026 en el espacio con id 1"));

        String jsonBody = """
                {"anio": 2026, "mes": 9}
                """;

        mockMvc.perform(post("/api/v1/espacios/1/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * Prueba el endpoint de listado de periodos retornando codigo 200 OK.
     */
    @Test
    void listarPeriodos_espacioValido_retornaOkConLista() throws Exception {
        PeriodoResponseDTO detalle = PeriodoResponseDTO.builder()
                .id(10L).espacioId(1L).anio(2026).mes(9).estado(EstadoPeriodo.ABIERTO).build();

        when(periodoService.listarPeriodos(1L)).thenReturn(List.of(detalle));

        mockMvc.perform(get("/api/v1/espacios/1/periodos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    /**
     * Prueba el endpoint de consulta de un periodo inexistente retornando codigo 404 Not Found.
     */
    @Test
    void obtenerPeriodo_inexistente_retornaNotFound() throws Exception {
        when(periodoService.obtenerPeriodoPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Periodo no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/espacios/1/periodos/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * Prueba el endpoint de cierre de periodo retornando codigo 200 OK con la liquidacion generada.
     */
    @Test
    void cerrarPeriodo_periodoAbierto_retornaOkConLiquidacion() throws Exception {
        LiquidacionResponseDTO liquidacion = LiquidacionResponseDTO.builder().id(50L).periodoId(10L).build();
        when(periodoService.cerrarPeriodo(10L)).thenReturn(liquidacion);

        mockMvc.perform(post("/api/v1/espacios/1/periodos/10/cierre"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50L));
    }

    /**
     * Prueba el endpoint de cierre de un periodo ya cerrado retornando codigo 400 Bad Request.
     */
    @Test
    void cerrarPeriodo_yaCerrado_retornaBadRequest() throws Exception {
        when(periodoService.cerrarPeriodo(10L))
                .thenThrow(new ReglaInvalidaException("El periodo con id 10 ya se encuentra cerrado"));

        mockMvc.perform(post("/api/v1/espacios/1/periodos/10/cierre"))
                .andExpect(status().isBadRequest());
    }
}
