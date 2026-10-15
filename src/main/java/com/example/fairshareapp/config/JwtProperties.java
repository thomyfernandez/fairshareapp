package com.example.fairshareapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuracion para la generacion y validacion de tokens JWT.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Clave secreta para la firma y verificacion de los tokens JWT.
     */
    private String secret = "fairshare-super-secret-key-that-is-at-least-32-bytes-long";

    /**
     * Tiempo de expiracion de los tokens JWT en milisegundos.
     */
    private Long expirationMs = 3600000L;
}
