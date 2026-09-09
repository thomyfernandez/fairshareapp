package com.example.fairshareapp.controller;

import com.example.fairshareapp.exception.CredencialesInvalidasException;
import com.example.fairshareapp.exception.EmailYaRegistradoException;
import com.example.fairshareapp.exception.GlobalExceptionHandler;
import com.example.fairshareapp.model.request.LoginRequest;
import com.example.fairshareapp.model.request.RegistroUsuarioRequest;
import com.example.fairshareapp.model.response.UsuarioResponse;
import com.example.fairshareapp.service.UsuarioService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para UsuarioController usando MockMvc.
 * Valida los codigos de estado HTTP y enrutamiento para registro, login y consulta de usuarios.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    /**
     * Inicializa MockMvc en modo standalone configurando el manejador global de excepciones.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Valida el registro exitoso de un nuevo usuario retornando codigo 201 Created.
     */
    @Test
    void registro_payloadValido_retornaCreated() throws Exception {
        doNothing().when(usuarioService).registrarUsuario(any(RegistroUsuarioRequest.class));

        String json = """
                {
                    "nombre": "Carlos",
                    "apellido": "Lopez",
                    "email": "carlos@example.com",
                    "contra": "password123",
                    "usuario": "clopez"
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));
    }

    /**
     * Valida que el registro retorne 409 Conflict cuando el email ya se encuentra registrado.
     */
    @Test
    void registro_emailDuplicado_retornaConflict() throws Exception {
        doThrow(new EmailYaRegistradoException("carlos@example.com"))
                .when(usuarioService).registrarUsuario(any(RegistroUsuarioRequest.class));

        String json = """
                {
                    "nombre": "Carlos",
                    "apellido": "Lopez",
                    "email": "carlos@example.com",
                    "contra": "password123",
                    "usuario": "clopez"
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    /**
     * Valida el inicio de sesion correcto retornando codigo 200 OK.
     */
    @Test
    void login_credencialesValidas_retornaOk() throws Exception {
        doNothing().when(usuarioService).loginUsuario(any(LoginRequest.class));

        String json = """
                {
                    "email": "carlos@example.com",
                    "contra": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    /**
     * Valida que el login retorne 401 Unauthorized ante credenciales incorrectas.
     */
    @Test
    void login_credencialesInvalidas_retornaUnauthorized() throws Exception {
        doThrow(new CredencialesInvalidasException())
                .when(usuarioService).loginUsuario(any(LoginRequest.class));

        String json = """
                {
                    "email": "carlos@example.com",
                    "contra": "incorrecta"
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Valida la obtencion del listado de usuarios retornando codigo 200 OK y la lista esperada.
     */
    @Test
    void getAllUsuarios_retornaOkConLista() throws Exception {
        UsuarioResponse user1 = new UsuarioResponse("clopez", "carlos@example.com", "Carlos", "Lopez");
        UsuarioResponse user2 = new UsuarioResponse("mgomez", "maria@example.com", "Maria", "Gomez");

        when(usuarioService.getAllUsuarios()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuario").value("clopez"))
                .andExpect(jsonPath("$[0].email").value("carlos@example.com"))
                .andExpect(jsonPath("$[1].usuario").value("mgomez"));
    }

    /**
     * Valida la compatibilidad con la ruta anterior /api/usuario/get retornando 200 OK.
     */
    @Test
    void getAllUsuarios_rutaLegacy_retornaOk() throws Exception {
        UsuarioResponse user = new UsuarioResponse("clopez", "carlos@example.com", "Carlos", "Lopez");
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/usuario/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuario").value("clopez"));
    }
}
