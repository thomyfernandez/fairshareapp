package com.example.fairshareapp.exception;

import com.example.fairshareapp.model.dto.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de GlobalExceptionHandler: verifican que cada categoria de excepcion
 * se traduzca al codigo HTTP y al contrato ErrorResponseDTO correctos.
 *
 * Se combinan dos estrategias:
 * - Llamadas directas a los metodos del handler (con un MockHttpServletRequest) para las
 *   excepciones de negocio, donde no hace falta el despacho real de Spring MVC.
 * - MockMvc sobre un controlador de prueba dedicado para los casos que dependen de la
 *   mecanica real de Spring MVC (binding de @Valid, parseo de JSON, metodo HTTP no
 *   soportado, tipo de contenido no soportado), que no pueden simularse invocando el
 *   metodo del handler directamente.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/recurso/1");

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExcepcionesTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recursoNoEncontrado_retorna404ConCodigoYPath() {
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleRecursoNoEncontrado(new RecursoNoEncontradoException("Espacio no encontrado con id: 1"), request);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("RECURSO_NO_ENCONTRADO", respuesta.getBody().getCode());
        assertEquals("/api/v1/recurso/1", respuesta.getBody().getPath());
        assertNotNull(respuesta.getBody().getTimestamp());
    }

    @Test
    void reglaInvalida_retorna400() {
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleReglaInvalida(new ReglaInvalidaException("El monto debe ser positivo"), request);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("REGLA_INVALIDA", respuesta.getBody().getCode());
    }

    @Test
    void credencialesInvalidas_retorna401() {
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleCredencialesInvalidas(new CredencialesInvalidasException(), request);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals("CREDENCIALES_INVALIDAS", respuesta.getBody().getCode());
    }

    @Test
    void accesoDenegado_retorna403() {
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleAccesoDenegado(new AccesoDenegadoException("Se requieren permisos de administrador"), request);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertEquals("ACCESO_DENEGADO", respuesta.getBody().getCode());
    }

    @Test
    void emailYaRegistrado_esConflictoYRetorna409() {
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleConflicto(new EmailYaRegistradoException("juan@test.com"), request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("CONFLICTO", respuesta.getBody().getCode());
    }

    @Test
    void membresiaDuplicada_esConflictoYRetorna409() {
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleConflicto(new MembresiaDuplicadaException("Ya es miembro del espacio"), request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("CONFLICTO", respuesta.getBody().getCode());
    }

    @Test
    void integridadDatos_conMensajeDeUnicidad_retorna409() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Unique index or primary key violation en usuario.email");

        ResponseEntity<ErrorResponseDTO> respuesta = handler.handleIntegridadDatos(ex, request);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("RECURSO_DUPLICADO", respuesta.getBody().getCode());
    }

    @Test
    void integridadDatos_sinIndicioDeUnicidad_retorna400NoSeClasificaComoDuplicado() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Cannot delete or update a parent row: a foreign key constraint fails");

        ResponseEntity<ErrorResponseDTO> respuesta = handler.handleIntegridadDatos(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("INTEGRIDAD_DATOS", respuesta.getBody().getCode());
    }

    @Test
    void constraintViolation_retorna400ConCodigoValidationError() {
        ConstraintViolationException ex = new ConstraintViolationException("Parametros invalidos", Collections.emptySet());

        ResponseEntity<ErrorResponseDTO> respuesta = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("VALIDATION_ERROR", respuesta.getBody().getCode());
    }

    @Test
    void errorInesperado_retorna500SinDetallesInternos() {
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleGenericException(new RuntimeException("boom, detalle interno sensible"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertEquals("ERROR_INTERNO", respuesta.getBody().getCode());
        assertEquals("Ocurrio un error inesperado", respuesta.getBody().getMessage());
    }

    // ==================== Casos que requieren despacho real de Spring MVC ====================

    /**
     * Un DTO invalido debe retornar 400 con el detalle de todos los campos rechazados.
     */
    @Test
    void dtoInvalido_retorna400ConDetalleDeCampos() throws Exception {
        String json = """
                {
                    "nombre": "",
                    "email": "no-es-un-email",
                    "tipo": "A"
                }
                """;

        mockMvc.perform(post("/test/validar").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    /**
     * Un JSON mal formado debe retornar 400 sin exponer detalles tecnicos de deserializacion.
     */
    @Test
    void jsonMalFormado_retorna400() throws Exception {
        mockMvc.perform(post("/test/validar").contentType(MediaType.APPLICATION_JSON).content("{ invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JSON_MALFORMADO"));
    }

    /**
     * Un valor de enum que no corresponde a ninguna constante valida debe retornar 400.
     */
    @Test
    void enumInvalidoEnBody_retorna400() throws Exception {
        String json = """
                {
                    "nombre": "Juan",
                    "email": "juan@test.com",
                    "tipo": "NO_EXISTE"
                }
                """;

        mockMvc.perform(post("/test/validar").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JSON_MALFORMADO"));
    }

    /**
     * La ausencia de un parametro obligatorio debe retornar 400 con el nombre del parametro.
     */
    @Test
    void parametroObligatorioAusente_retorna400() throws Exception {
        mockMvc.perform(get("/test/parametro-obligatorio"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETRO_FALTANTE"))
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());
    }

    /**
     * Un path variable con un tipo que no puede convertirse debe retornar 400.
     */
    @Test
    void tipoDeParametroInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/test/tipo-invalido/no-es-un-numero"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"));
    }

    /**
     * Un metodo HTTP no soportado por el endpoint debe preservarse como 405, no como 500.
     */
    @Test
    void metodoHttpNoPermitido_retorna405() throws Exception {
        mockMvc.perform(post("/test/parametro-obligatorio"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METODO_NO_PERMITIDO"));
    }

    /**
     * Un tipo de contenido no soportado por el endpoint debe preservarse como 415, no como 500.
     */
    @Test
    void mediaTypeNoSoportado_retorna415() throws Exception {
        mockMvc.perform(post("/test/validar").contentType(MediaType.TEXT_PLAIN).content("texto plano"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("MEDIA_TYPE_NO_SOPORTADO"));
    }

    // ==================== Fixtures de prueba ====================

    @RestController
    @RequestMapping("/test")
    static class ExcepcionesTestController {

        @PostMapping("/validar")
        public ResponseEntity<Void> validar(@Valid @RequestBody DatoValidableDTO dto) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/parametro-obligatorio")
        public ResponseEntity<Void> parametroObligatorio(@RequestParam String nombre) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/tipo-invalido/{id}")
        public ResponseEntity<Void> tipoInvalido(@PathVariable Long id) {
            return ResponseEntity.ok().build();
        }
    }

    private record DatoValidableDTO(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @Email(message = "Debe tener formato de email valido") String email,
            TipoDeTest tipo) {
    }

    private enum TipoDeTest { A, B }
}
