package com.example.fairshareapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @org.springframework.beans.factory.annotation.Value("${app.cors-origins:http://localhost:5173,http://localhost:3000}")
    private String[] origins;

    /**
     * CORS para el frontend cuando no pasa por un proxy del mismo origen.
     * allowCredentials(true) permite que el navegador envie la cookie de sesion; por eso
     * app.cors-origins debe listar origenes explicitos (nunca "*").
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
