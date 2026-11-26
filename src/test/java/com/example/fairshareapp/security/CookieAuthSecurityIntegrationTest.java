package com.example.fairshareapp.security;

import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion de la cadena de Spring Security completa:
 * login con cookie HttpOnly, rehidratacion por /me, proteccion CSRF y logout.
 */
@SpringBootTest
class CookieAuthSecurityIntegrationTest {

    private static final String EMAIL = "cookie_test@example.com";
    private static final String CONTRA = "password123";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        if (usuarioRepository.findByEmailIgnoreCase(EMAIL).isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .nombreUsuario("cookie_test")
                    .email(EMAIL)
                    .contra(new BCryptPasswordEncoder().encode(CONTRA))
                    .nombre("Cookie")
                    .apellido("Test")
                    .build());
        }
    }

    private Cookie login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"contra\":\"" + CONTRA + "\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("fairshare_token", true))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();
        Cookie sesion = result.getResponse().getCookie("fairshare_token");
        assertNotNull(sesion);
        return sesion;
    }

    /** Obtiene la cookie XSRF-TOKEN igual que el frontend: GET /csrf. */
    private Cookie xsrfCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/usuarios/csrf")).andReturn();
        Cookie xsrf = result.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(xsrf);
        return xsrf;
    }

    @Test
    void postProtegido_conHeaderCsrfQueNoCoincideConLaCookie_retorna403() throws Exception {
        Cookie sesion = login();
        Cookie xsrf = xsrfCookie();

        mockMvc.perform(post("/api/v1/espacios").cookie(sesion, xsrf).header("X-XSRF-TOKEN", "token-adivinado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointProtegido_sinCookie_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/espacios"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_conCookieDeSesion_rehidrataUsuario() throws Exception {
        Cookie sesion = login();

        mockMvc.perform(get("/api/v1/usuarios/me").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void cookieConTokenInvalido_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me").cookie(new Cookie("fairshare_token", "token-falso")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postProtegido_conCookiePeroSinCsrf_retorna403() throws Exception {
        Cookie sesion = login();

        mockMvc.perform(post("/api/v1/espacios").cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postProtegido_conCookieYCsrf_superaLaSeguridad() throws Exception {
        Cookie sesion = login();

        // Payload invalido a proposito: llegar a la validacion (400) prueba que se pasaron auth y CSRF.
        Cookie xsrf = xsrfCookie();

        mockMvc.perform(post("/api/v1/espacios").cookie(sesion, xsrf).header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cookieVencidaOInvalida_noImpideVolverAIniciarSesion() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login").cookie(new Cookie("fairshare_token", "token-viejo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"contra\":\"" + CONTRA + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void clienteBearer_noNecesitaCsrf() throws Exception {
        Cookie sesion = login();

        // Un sitio externo no puede agregar el header Authorization: no hay riesgo de CSRF.
        mockMvc.perform(post("/api/v1/espacios").header("Authorization", "Bearer " + sesion.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void csrf_emiteCookieXsrfLegibleParaElFrontend() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/csrf"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("XSRF-TOKEN=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("HttpOnly"))))
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"));
    }

    @Test
    void logout_conCsrf_borraLaCookie() throws Exception {
        Cookie sesion = login();

        Cookie xsrf = xsrfCookie();

        mockMvc.perform(post("/api/v1/usuarios/logout").cookie(sesion, xsrf).header("X-XSRF-TOKEN", xsrf.getValue()))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}
