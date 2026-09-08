package com.example.fairshareapp.config;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de seguridad web para autorizacion de endpoints REST.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * Se deshabilita CSRF debido a que la API esta disenada como un servicio REST sin estado (stateless),
     * donde la autenticacion no se basa en cookies de sesion del navegador.
     *
     * @param http Configurador de seguridad HTTP.
     * @return Cadena de filtros de seguridad construida.
     */
    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http
                // Deshabilitado de forma segura ya que el servicio opera de manera stateless
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
                );
            return http.build();
        } catch (Exception e) {
            throw new BeanInitializationException("Error al configurar la cadena de filtros de seguridad", e);
        }
    }
}
