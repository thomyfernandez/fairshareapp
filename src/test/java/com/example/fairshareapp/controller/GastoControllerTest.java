package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.enums.ReglaDivision;
import com.example.fairshareapp.service.GastoService;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador GastoController usando MockMvc.
 * Valida el enrutamiento y codigos de estado HTTP de los endpoints REST expuestos.
 */
@ExtendWith(MockitoExtension.class)
class GastoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GastoService gastoService;

    @InjectMocks
    private GastoController gastoController;

    /**
     * Inicializa el entorno de prueba aislado de MockMvc con el manejador global de excepciones.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gastoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Prueba el endpoint de registro de gasto retornando codigo 201 Created.
     */
    @Test
    void registrarGasto_PayloadValido_RetornaCreated() throws Exception {
        GastoDetalleDTO detalle = GastoDetalleDTO.builder()
                .id(1L)
                .descripcion("Cena")
                .monto(BigDecimal.valueOf(100.0))
                .espacioId(10L)
                .regla(ReglaDivision.EQUITATIVA)
                .participantes(Collections.emptyList())
                .build();

        when(gastoService.registrarGasto(eq(10L), any(CrearGastoDTO.class))).thenReturn(detalle);

        String jsonBody = """
                {
                    "descripcion": "Cena",
                    "monto": 100.0,
                    "pagadorId": 1,
                    "regla": "EQUITATIVA",
                    "participantes": [{"usuarioId": 1}]
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/10/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descripcion").value("Cena"))
                .andExpect(jsonPath("$.monto").value(100.0));
    }

    /**
     * Prueba el endpoint de registro de lote de gastos retornando codigo 201 Created.
     */
    @Test
    void registrarLoteGastos_PayloadValido_RetornaCreated() throws Exception {
        GastoDetalleDTO detalle = GastoDetalleDTO.builder()
                .id(1L)
                .descripcion("Lote 1")
                .monto(BigDecimal.valueOf(250.0))
                .build();

        when(gastoService.registrarLoteGastos(eq(10L), any())).thenReturn(List.of(detalle));

        String jsonBody = """
                [
                    {
                        "descripcion": "Lote 1",
                        "monto": 250.0,
                        "pagadorId": 1,
                        "regla": "EQUITATIVA",
                        "participantes": [{"usuarioId": 1}]
                    }
                ]
                """;

        mockMvc.perform(post("/api/v1/espacios/10/gastos/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].descripcion").value("Lote 1"));
    }

    /**
     * Prueba la obtencion de gastos de un espacio retornando codigo 200 OK.
     */
    @Test
    void obtenerGastosPorEspacio_RetornaOkConLista() throws Exception {
        GastoDetalleDTO detalle = GastoDetalleDTO.builder()
                .id(5L)
                .descripcion("Gasto recuperado")
                .monto(BigDecimal.valueOf(300.0))
                .fecha(LocalDate.of(2026, 9, 7))
                .build();

        when(gastoService.obtenerGastosPorEspacioYPeriodo(eq(10L), any(), any()))
                .thenReturn(List.of(detalle));

        mockMvc.perform(get("/api/v1/espacios/10/gastos")
                        .param("desde", "2026-09-01")
                        .param("hasta", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].descripcion").value("Gasto recuperado"));
    }

    /**
     * Prueba la eliminacion exitosa de un gasto retornando codigo 204 No Content.
     */
    @Test
    void eliminarGasto_IdExistente_RetornaNoContent() throws Exception {
        doNothing().when(gastoService).eliminarGasto(1L);

        mockMvc.perform(delete("/api/v1/gastos/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * Prueba la eliminacion de un gasto inexistente retornando codigo 404 Not Found.
     */
    @Test
    void eliminarGasto_IdInexistente_RetornaNotFound() throws Exception {
        doThrow(new RecursoNoEncontradoException("Gasto no encontrado con id: 99"))
                .when(gastoService).eliminarGasto(99L);

        mockMvc.perform(delete("/api/v1/gastos/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * Valida que el intento de registrar un gasto sin descripcion retorne codigo 400 Bad Request por validacion.
     */
    @Test
    void registrarGasto_DescripcionVacia_RetornaBadRequest() throws Exception {
        String jsonBody = """
                {
                    "descripcion": "",
                    "monto": 100.0,
                    "pagadorId": 1,
                    "regla": "EQUITATIVA",
                    "participantes": [{"usuarioId": 1}]
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/10/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * Valida que el intento de registrar un gasto con monto negativo retorne codigo 400 Bad Request.
     */
    @Test
    void registrarGasto_MontoNegativo_RetornaBadRequest() throws Exception {
        String jsonBody = """
                {
                    "descripcion": "Supermercado",
                    "monto": -50.0,
                    "pagadorId": 1,
                    "regla": "EQUITATIVA",
                    "participantes": [{"usuarioId": 1}]
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/10/gastos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }
}
