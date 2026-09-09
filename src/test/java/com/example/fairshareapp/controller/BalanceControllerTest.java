package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.BalanceDTO;
import com.example.fairshareapp.model.dto.DeudaDetalleDTO;
import com.example.fairshareapp.model.enums.EstadoDeuda;
import com.example.fairshareapp.service.BalanceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador BalanceController usando MockMvc.
 * Valida el enrutamiento y codigos de estado HTTP de los endpoints REST expuestos.
 */
@ExtendWith(MockitoExtension.class)
class BalanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private BalanceController balanceController;

    /**
     * Inicializa el entorno de prueba aislado de MockMvc con el manejador global de excepciones.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(balanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Prueba el endpoint de balance retornando codigo 200 OK con la matriz simplificada.
     */
    @Test
    void obtenerBalance_espacioValido_retornaOk() throws Exception {
        DeudaDetalleDTO deuda = DeudaDetalleDTO.builder()
                .id(1L)
                .deudorId(2L)
                .deudorNombre("Maria Gomez")
                .acreedorId(1L)
                .acreedorNombre("Juan Perez")
                .monto(BigDecimal.valueOf(500.0))
                .estado(EstadoDeuda.PENDIENTE)
                .build();

        BalanceDTO balance = BalanceDTO.builder()
                .espacioId(1L)
                .espacioNombre("Depto Palermo")
                .deudas(List.of(deuda))
                .build();

        when(balanceService.obtenerBalance(1L)).thenReturn(balance);

        mockMvc.perform(get("/api/v1/espacios/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.espacioId").value(1L))
                .andExpect(jsonPath("$.deudas[0].monto").value(500.0));
    }

    /**
     * Prueba el endpoint de balance retornando codigo 404 Not Found ante un espacio inexistente.
     */
    @Test
    void obtenerBalance_espacioInexistente_retornaNotFound() throws Exception {
        when(balanceService.obtenerBalance(99L))
                .thenThrow(new RecursoNoEncontradoException("Espacio no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/espacios/99/balance"))
                .andExpect(status().isNotFound());
    }

    /**
     * Prueba el endpoint de saldar deuda retornando codigo 200 OK con el pago total.
     */
    @Test
    void saldarDeuda_pagoTotal_retornaOk() throws Exception {
        DeudaDetalleDTO deuda = DeudaDetalleDTO.builder()
                .id(1L)
                .estado(EstadoDeuda.SALDADO)
                .monto(BigDecimal.ZERO)
                .build();

        when(balanceService.registrarPago(eq(1L), any())).thenReturn(deuda);

        mockMvc.perform(post("/api/v1/deudas/1/saldar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SALDADO"));
    }

    /**
     * Prueba el endpoint de saldar deuda retornando codigo 400 Bad Request cuando la regla de negocio falla.
     */
    @Test
    void saldarDeuda_deudaYaSaldada_retornaBadRequest() throws Exception {
        when(balanceService.registrarPago(eq(1L), any()))
                .thenThrow(new ReglaInvalidaException("La deuda con id 1 ya se encuentra saldada"));

        mockMvc.perform(post("/api/v1/deudas/1/saldar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
