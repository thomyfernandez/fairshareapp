package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.dto.EspacioCreateDTO;
import com.example.fairshareapp.model.dto.EspacioResponseDTO;
import com.example.fairshareapp.model.dto.EspacioUpdateDTO;
import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import com.example.fairshareapp.service.EspacioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador EspacioController usando MockMvc.
 * Valida los endpoints REST POST, GET y PUT en /api/v1/espacios.
 */
@ExtendWith(MockitoExtension.class)
class EspacioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EspacioService espacioService;

    @InjectMocks
    private EspacioController espacioController;

    private EspacioResponseDTO respuestaMock;

    /**
     * Inicializa el entorno de prueba aislado de MockMvc con el controlador y GlobalExceptionHandler.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(espacioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        respuestaMock = EspacioResponseDTO.builder()
                .id(1L)
                .nombre("Depto Belgrano")
                .descripcion("Espacio para convivencia")
                .codigo("DEPTO-BEL")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .presupuestoBase(BigDecimal.valueOf(100000.00))
                .build();
    }

    /**
     * Valida la creacion de un espacio retornando codigo HTTP 201 Created.
     */
    @Test
    void crearEspacio_DatosValidos_RetornaCreated() throws Exception {
        when(espacioService.crearEspacio(any(EspacioCreateDTO.class))).thenReturn(respuestaMock);

        String jsonPayload = """
                {
                    "nombre": "Depto Belgrano",
                    "descripcion": "Espacio para convivencia",
                    "codigo": "DEPTO-BEL",
                    "tipo": "HOGAR",
                    "reglaReparto": "CINCUENTA_CINCUENTA",
                    "presupuestoBase": 100000.00
                }
                """;

        mockMvc.perform(post("/api/v1/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Depto Belgrano"))
                .andExpect(jsonPath("$.codigo").value("DEPTO-BEL"))
                .andExpect(jsonPath("$.tipo").value("HOGAR"))
                .andExpect(jsonPath("$.reglaReparto").value("CINCUENTA_CINCUENTA"));
    }

    /**
     * Valida que una solicitud con nombre vacio sea rechazada con codigo HTTP 400 Bad Request.
     */
    @Test
    void crearEspacio_NombreVacio_RetornaBadRequest() throws Exception {
        String jsonInvalido = """
                {
                    "nombre": "",
                    "tipo": "HOGAR",
                    "reglaReparto": "CINCUENTA_CINCUENTA"
                }
                """;

        mockMvc.perform(post("/api/v1/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    /**
     * Valida la consulta de un espacio por su id retornando codigo HTTP 200 OK.
     */
    @Test
    void obtenerEspacioPorId_Existente_RetornaOk() throws Exception {
        when(espacioService.obtenerEspacioPorId(1L)).thenReturn(respuestaMock);

        mockMvc.perform(get("/api/v1/espacios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Depto Belgrano"));
    }

    /**
     * Valida que al consultar un id inexistente se retorne codigo HTTP 404 Not Found.
     */
    @Test
    void obtenerEspacioPorId_Inexistente_RetornaNotFound() throws Exception {
        when(espacioService.obtenerEspacioPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Espacio no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/espacios/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * Valida la actualizacion completa de un espacio retornando codigo HTTP 200 OK.
     */
    @Test
    void actualizarEspacio_DatosValidos_RetornaOk() throws Exception {
        EspacioResponseDTO respuestaActualizada = EspacioResponseDTO.builder()
                .id(1L)
                .nombre("Depto Belgrano Modificado")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.PROPORCIONAL)
                .presupuestoBase(BigDecimal.valueOf(200000.00))
                .build();

        when(espacioService.actualizarEspacio(eq(1L), any(EspacioUpdateDTO.class))).thenReturn(respuestaActualizada);

        String jsonUpdate = """
                {
                    "nombre": "Depto Belgrano Modificado",
                    "reglaReparto": "PROPORCIONAL",
                    "presupuestoBase": 200000.00
                }
                """;

        mockMvc.perform(put("/api/v1/espacios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Depto Belgrano Modificado"))
                .andExpect(jsonPath("$.reglaReparto").value("PROPORCIONAL"))
                .andExpect(jsonPath("$.presupuestoBase").value(200000.00));
    }

    /**
     * Valida el listado de espacios retornando codigo HTTP 200 OK.
     */
    @Test
    void listarEspacios_RetornaOkConLista() throws Exception {
        when(espacioService.listarEspacios()).thenReturn(List.of(respuestaMock));

        mockMvc.perform(get("/api/v1/espacios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Depto Belgrano"));
    }

    /**
     * Valida la modificacion directa de la regla de distribucion mediante PATCH.
     */
    @Test
    void editarReglaDistribucion_RetornaOk() throws Exception {
        EspacioResponseDTO respuestaRegla = EspacioResponseDTO.builder()
                .id(1L)
                .reglaReparto(ReglaReparto.PROPORCIONAL)
                .build();

        when(espacioService.editarReglaDistribucion(1L, ReglaReparto.PROPORCIONAL)).thenReturn(respuestaRegla);

        mockMvc.perform(patch("/api/v1/espacios/1/regla-distribucion")
                        .param("reglaReparto", "PROPORCIONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reglaReparto").value("PROPORCIONAL"));
    }

    /**
     * Valida la fijacion del presupuesto base mediante PATCH.
     */
    @Test
    void fijarPresupuestoBase_RetornaOk() throws Exception {
        EspacioResponseDTO respuestaPresupuesto = EspacioResponseDTO.builder()
                .id(1L)
                .presupuestoBase(BigDecimal.valueOf(180000.00))
                .build();

        when(espacioService.fijarPresupuestoBase(eq(1L), any(BigDecimal.class))).thenReturn(respuestaPresupuesto);

        mockMvc.perform(patch("/api/v1/espacios/1/presupuesto-base")
                        .param("presupuestoBase", "180000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presupuestoBase").value(180000.00));
    }
}
