package com.example.fairshareapp.config;
import com.example.fairshareapp.security.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public FilterRegistrationBean<JwtAuthenticationFilter> jwtRegistration(JwtAuthenticationFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); return registration;
    }
    /**
     * Configura la cadena de filtros de seguridad HTTP, autorizaciones de rutas y politica stateless.
     * La proteccion CSRF se deshabilita de forma intencional y segura ya que la aplicacion
     * utiliza exclusivamente autenticacion stateless mediante tokens JWT Bearer sin cookies de sesion.
     *
     * @param http configurador de seguridad HTTP
     * @param jwt filtro de autenticacion JWT
     * @param errors manejador de excepciones de seguridad
     * @return cadena de filtros de seguridad construida
     */
    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwt, SecurityErrorHandler errors) {
        return http.cors(cors -> {}).csrf(csrf -> csrf.disable()) // Bearer explicito; no sesion ni cookies.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(c -> c.disable()).formLogin(f -> f.disable()).httpBasic(b -> b.disable()).logout(l -> l.disable())
            .exceptionHandling(e -> e.authenticationEntryPoint(errors).accessDeniedHandler(errors))
            .authorizeHttpRequests(a -> a
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/status", "/api/hello", "/error").permitAll()
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/usuarios/registro", "/api/v1/usuarios/login", "/api/usuario/registro", "/api/usuario/login").permitAll()
                .requestMatchers("/api/v1/usuarios/me", "/api/usuario/me").authenticated()
                .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/get", "/api/usuario", "/api/usuario/get").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }
}
