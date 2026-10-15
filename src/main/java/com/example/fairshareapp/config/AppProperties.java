package com.example.fairshareapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades globales de configuracion de la aplicacion FairShare.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Origenes permitidos para las solicitudes CORS.
     */
    private String corsOrigins = "http://localhost:5173,http://localhost:3000";

    /**
     * Configuracion del usuario administrador por defecto para inicializacion.
     */
    private AdminProperties admin = new AdminProperties();

    /**
     * Propiedades anidadas para el usuario administrador del sistema.
     */
    @Getter
    @Setter
    public static class AdminProperties {
        /**
         * Correo electronico del usuario administrador.
         */
        private String email = "admin@fairshare.com";

        /**
         * Contrasena del usuario administrador.
         */
        private String password = "Admin123!";
    }
}
