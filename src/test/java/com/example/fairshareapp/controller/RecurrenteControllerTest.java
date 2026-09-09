package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarPrecioCicloDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.service.RecurrentesService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para RecurrenteController usando MockMvc.
 * Valida los codigos de estado HTTP y respuestas para la administracion de gastos favoritos y recurrentes.
 */
@ExtendWith(MockitoExtension.class)
class RecurrenteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecurrentesService recurrentesService;

    @InjectMocks
    private RecurrenteController recurrenteController;

    /**
     * Inicializa el entorno aislado de pruebas para el controlador.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recurrenteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Valida la creacion de un favorito retornando codigo 201 Created.
     */
    @Test
    void crearFavorito_valido_retornaCreated() throws Exception {
        PlantillaGastoDTO response = PlantillaGastoDTO.builder()
                .id(1L)
                .nombre("Alquiler")
                .espacioId(1L)
                .montoBase(BigDecimal.valueOf(250000.00))
                .build();

        when(recurrentesService.guardarFavorito(any(PlantillaGastoDTO.class))).thenReturn(response);

        String json = """
                {
                    "nombre": "Alquiler",
                    "frecuenciaAjusteMeses": 3,
                    "montoBase": 250000.00,
                    "montoVariable": 0.00,
                    "fechaProximaRevision": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/1/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Alquiler"));
    }

    /**
     * Valida la obtencion de la lista de favoritos de un espacio con codigo 200 OK.
     */
    @Test
    void obtenerFavoritos_retornaOk() throws Exception {
        PlantillaGastoDTO item = PlantillaGastoDTO.builder()
                .id(1L)
                .nombre("Internet")
                .espacioId(1L)
                .vencido(false)
                .build();

        when(recurrentesService.obtenerFavoritosPorEspacio(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/espacios/1/favoritos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Internet"));
    }

    /**
     * Valida el filtrado de plantillas favoritas vencidas mediante el parametro soloVencidos.
     */
    @Test
    void obtenerFavoritos_conFiltroSoloVencidos_retornaSoloVencidos() throws Exception {
        PlantillaGastoDTO itemVigente = PlantillaGastoDTO.builder()
                .id(1L)
                .nombre("Internet")
                .espacioId(1L)
                .vencido(false)
                .build();
        PlantillaGastoDTO itemVencido = PlantillaGastoDTO.builder()
                .id(2L)
                .nombre("Alquiler")
                .espacioId(1L)
                .vencido(true)
                .build();

        when(recurrentesService.obtenerFavoritosPorEspacio(1L)).thenReturn(List.of(itemVigente, itemVencido));

        mockMvc.perform(get("/api/v1/espacios/1/favoritos?soloVencidos=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].nombre").value("Alquiler"));
    }

    /**
     * Valida la consulta especifica de vencimientos con codigo 200 OK.
     */
    @Test
    void obtenerVencimientos_retornaOk() throws Exception {
        ServicioVencimientoDTO vencido = ServicioVencimientoDTO.builder()
                .servicioId(1L)
                .nombreServicio("Gas Natural")
                .fechaVencimiento(LocalDate.now().minusDays(3))
                .diasVencido(3L)
                .build();

        when(recurrentesService.detectarVencimientos(1L)).thenReturn(List.of(vencido));

        mockMvc.perform(get("/api/v1/espacios/1/favoritos/vencimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreServicio").value("Gas Natural"))
                .andExpect(jsonPath("$[0].diasVencido").value(3));
    }

    /**
     * Valida el disparo de ejecucion rapida en 1 clic retornando codigo 201 Created.
     */
    @Test
    void ejecutarGasto_retornaCreated() throws Exception {
        GastoDetalleDTO detalle = GastoDetalleDTO.builder()
                .id(50L)
                .descripcion("Alquiler")
                .monto(250000.0)
                .build();

        when(recurrentesService.ejecutarGastoDesdePlantilla(1L)).thenReturn(detalle);

        mockMvc.perform(post("/api/v1/favoritos/1/ejecutar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50L))
                .andExpect(jsonPath("$.descripcion").value("Alquiler"));
    }

    /**
     * Valida que si la tarifa de ciclo esta vencida, la ejecucion se bloquee con 400 Bad Request.
     */
    @Test
    void ejecutarGasto_conTarifaVencida_retornaBadRequest() throws Exception {
        when(recurrentesService.ejecutarGastoDesdePlantilla(1L))
                .thenThrow(new ReglaInvalidaException("Debe actualizar la tarifa antes de ejecutar el gasto"));

        mockMvc.perform(post("/api/v1/favoritos/1/ejecutar"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Valida la actualizacion de importe de ciclo retornando 200 OK.
     */
    @Test
    void actualizarMonto_retornaOk() throws Exception {
        PlantillaGastoDTO response = PlantillaGastoDTO.builder()
                .id(1L)
                .nombre("Alquiler")
                .montoBase(BigDecimal.valueOf(300000.00))
                .build();

        when(recurrentesService.actualizarMonto(eq(1L), any(ActualizarPrecioCicloDTO.class)))
                .thenReturn(response);

        String json = """
                {
                    "nuevoMontoBase": 300000.00,
                    "nuevoMontoVariable": 10000.00
                }
                """;

        mockMvc.perform(put("/api/v1/favoritos/1/actualizar-monto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoBase").value(300000.00));
    }

    /**
     * Valida la eliminacion de un favorito con codigo 204 No Content.
     */
    @Test
    void eliminarFavorito_retornaNoContent() throws Exception {
        doNothing().when(recurrentesService).eliminarFavorito(1L);

        mockMvc.perform(delete("/api/v1/favoritos/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * Valida que crear un favorito con nombre vacio retorne codigo 400 Bad Request por validacion.
     */
    @Test
    void crearFavorito_nombreVacio_retornaBadRequest() throws Exception {
        String json = """
                {
                    "nombre": "",
                    "montoBase": 1000.00
                }
                """;

        mockMvc.perform(post("/api/v1/espacios/1/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    /**
     * Valida que actualizar un ciclo con monto negativo retorne codigo 400 Bad Request por validacion.
     */
    @Test
    void actualizarMonto_montoNegativo_retornaBadRequest() throws Exception {
        String json = """
                {
                    "nuevoMontoBase": -100.00
                }
                """;

        mockMvc.perform(put("/api/v1/favoritos/1/actualizar-monto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
