package com.example.fairshareapp.config;
import com.example.fairshareapp.security.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    /** Login y registro ocurren antes de tener sesion: no hay cookie de sesion que un atacante pueda reutilizar. */
    private static final Set<String> RUTAS_SIN_CSRF = Set.of(
        "/api/v1/usuarios/login", "/api/usuario/login", "/api/v1/usuarios/registro", "/api/usuario/registro");

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public FilterRegistrationBean<JwtAuthenticationFilter> jwtRegistration(JwtAuthenticationFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); return registration;
    }

    /**
     * CSRF solo aplica a solicitudes que modifican datos y se autentican con la cookie de sesion,
     * porque el navegador la adjunta sola (tambien en un formulario de otro sitio). Las que envian
     * "Authorization: Bearer" no son vulnerables: un sitio externo no puede agregar ese header sin
     * pasar por CORS, que solo admite los origenes configurados.
     */
    static RequestMatcher requiereCsrf() {
        return (HttpServletRequest request) -> !METODOS_SEGUROS.contains(request.getMethod())
            && request.getHeader("Authorization") == null
            && !RUTAS_SIN_CSRF.contains(request.getRequestURI().substring(request.getContextPath().length()));
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP, autorizaciones de rutas y politica stateless.
     * El JWT llega en una cookie HttpOnly (navegador) o en el header Bearer (clientes de API).
     * Para la cookie se habilita CSRF con el patron "double submit": la cookie XSRF-TOKEN (legible
     * por JS) debe repetirse en el header X-XSRF-TOKEN; un sitio externo no puede leerla.
     *
     * @param http configurador de seguridad HTTP
     * @param jwt filtro de autenticacion JWT
     * @param errors manejador de excepciones de seguridad
     * @return cadena de filtros de seguridad construida
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwt, SecurityErrorHandler errors) {
        return http.cors(cors -> {})
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .requireCsrfProtectionMatcher(requiereCsrf()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(c -> c.disable()).formLogin(f -> f.disable()).httpBasic(b -> b.disable()).logout(l -> l.disable())
            .exceptionHandling(e -> e.authenticationEntryPoint(errors).accessDeniedHandler(errors))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/status", "/api/hello", "/error").permitAll()
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro", "/api/v1/usuarios/login", "/api/usuario/registro", "/api/usuario/login",
                    "/api/v1/usuarios/logout", "/api/usuario/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/csrf", "/api/usuario/csrf").permitAll()
                .requestMatchers("/api/v1/usuarios/me", "/api/usuario/me").authenticated()
                .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/get", "/api/usuario", "/api/usuario/get").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }
}
