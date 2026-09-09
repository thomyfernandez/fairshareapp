package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.service.LiquidacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador LiquidacionController usando MockMvc.
 * Valida el enrutamiento y codigos de estado HTTP de los endpoints REST expuestos.
 */
@ExtendWith(MockitoExtension.class)
class LiquidacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LiquidacionService liquidacionService;

    @InjectMocks
    private LiquidacionController liquidacionController;

    /**
     * Inicializa el entorno de prueba aislado de MockMvc con el manejador global de excepciones.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(liquidacionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Prueba el endpoint de listado de liquidaciones retornando codigo 200 OK.
     */
    @Test
    void listarLiquidaciones_espacioValido_retornaOkConLista() throws Exception {
        LiquidacionResponseDTO detalle = LiquidacionResponseDTO.builder()
                .id(50L).espacioId(1L).anio(2026).mes(9).montoTotalGastos(BigDecimal.valueOf(1000)).build();

        when(liquidacionService.listarPorEspacio(1L)).thenReturn(List.of(detalle));

        mockMvc.perform(get("/api/v1/espacios/1/liquidaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50L));
    }

    /**
     * Prueba el endpoint de previsualizacion de liquidacion retornando codigo 200 OK con id nulo.
     */
    @Test
    void previsualizarLiquidacion_periodoAbierto_retornaOkSinId() throws Exception {
        LiquidacionResponseDTO preview = LiquidacionResponseDTO.builder()
                .id(null).espacioId(1L).anio(2026).mes(9).montoTotalGastos(BigDecimal.valueOf(500)).build();

        when(liquidacionService.previsualizar(1L, 2026, 9)).thenReturn(preview);

        mockMvc.perform(get("/api/v1/espacios/1/liquidaciones/preview?anio=2026&mes=9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    /**
     * Prueba el endpoint de consulta de una liquidacion inexistente retornando codigo 404 Not Found.
     */
    @Test
    void obtenerLiquidacion_inexistente_retornaNotFound() throws Exception {
        when(liquidacionService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Liquidacion no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/liquidaciones/99"))
                .andExpect(status().isNotFound());
    }
}
