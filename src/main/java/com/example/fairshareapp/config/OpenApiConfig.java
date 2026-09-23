package com.example.fairshareapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion global de documentacion OpenAPI 3 y Swagger UI para la API REST.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_SEGURIDAD = "Bearer Authentication";

    /**
     * Construye y expone la configuracion OpenAPI con informacion descriptiva y soporte para tokens JWT Bearer.
     *
     * @return instancia configurada de OpenAPI
     */
    @Bean
    public OpenAPI fairShareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FairShare API - Gestion Compartida de Gastos")
                        .description("API REST para la division equitativa y ponderada de gastos grupales")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo FairShare")
                                .email("soporte@fairshareapp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_SEGURIDAD, new SecurityScheme()
                                .name(ESQUEMA_SEGURIDAD)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
