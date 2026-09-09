package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.LiquidacionResponseDTO;
import com.example.fairshareapp.model.dto.ProcesarLiquidacionDTO;
import com.example.fairshareapp.model.dto.ResumenCierreDTO;
import com.example.fairshareapp.service.LiquidacionService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para LiquidacionController usando MockMvc en modo standalone.
 */
@ExtendWith(MockitoExtension.class)
class LiquidacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LiquidacionService liquidacionService;

    @InjectMocks
    private LiquidacionController liquidacionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(liquidacionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Valida la ejecucion exitosa del cierre de liquidacion retornando HTTP 201 Created.
     */
    @Test
    void procesarCierre_Exitoso_RetornaCreated() throws Exception {
        LiquidacionResponseDTO responseDTO = LiquidacionResponseDTO.builder()
                .id(1L)
                .espacioId(10L)
                .espacioNombre("Espacio Test")
                .montoTotal(BigDecimal.valueOf(45000.00))
                .fechaLiquidacion(LocalDate.of(2026, 9, 8))
                .mes(9)
                .anio(2026)
                .cantidadGastos(3)
                .resumenCierre(ResumenCierreDTO.builder()
                        .totalLiquidado(BigDecimal.valueOf(45000.00))
                        .presupuestoAnterior(BigDecimal.valueOf(100000.00))
                        .presupuestoRestante(BigDecimal.valueOf(55000.00))
                        .cantidadGastosCerrados(3)
                        .fechaCierre(LocalDate.of(2026, 9, 8))
                        .build())
                .build();

        when(liquidacionService.procesarCierreLiquidacion(eq(10L), any(ProcesarLiquidacionDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/espacios/10/liquidaciones/cierre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mes\": 9, \"anio\": 2026, \"descripcion\": \"Cierre mensual\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.montoTotal").value(45000.00))
                .andExpect(jsonPath("$.cantidadGastos").value(3))
                .andExpect(jsonPath("$.resumenCierre.presupuestoRestante").value(55000.00));
    }

    /**
     * Valida que se retorne HTTP 400 Bad Request cuando la regla de negocio falla (ej. sin presupuesto).
     */
    @Test
    void procesarCierre_PresupuestoInsuficiente_RetornaBadRequest() throws Exception {
        when(liquidacionService.procesarCierreLiquidacion(eq(10L), any(ProcesarLiquidacionDTO.class)))
                .thenThrow(new ReglaInvalidaException("El total a liquidar supera el presupuesto disponible"));

        mockMvc.perform(post("/api/v1/espacios/10/liquidaciones/cierre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Valida la obtencion del historial de liquidaciones retornando HTTP 200 OK.
     */
    @Test
    void obtenerHistorial_Exitoso_RetornaOk() throws Exception {
        LiquidacionResponseDTO item = LiquidacionResponseDTO.builder()
                .id(1L)
                .espacioId(10L)
                .montoTotal(BigDecimal.valueOf(30000.00))
                .cantidadGastos(2)
                .build();

        when(liquidacionService.obtenerHistorial(eq(10L), any(), any(), any(), any()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/espacios/10/liquidaciones/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].montoTotal").value(30000.00));
    }
}
