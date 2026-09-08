package com.example.fairshareapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de seguridad web para autorizacion de endpoints REST.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad HTTP, deshabilitando CSRF para APIs REST
     * y permitiendo el acceso publico a las rutas de la aplicacion durante la fase de desarrollo y pruebas.
     *
     * @param http Configurador de seguridad HTTP.
     * @return Cadena de filtros de seguridad construida.
     * @throws Exception Si ocurre un fallo en la configuracion.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
