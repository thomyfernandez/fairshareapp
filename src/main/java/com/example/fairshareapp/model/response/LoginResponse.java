package com.example.fairshareapp.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Resultado del login. El servicio genera el token y el controlador lo entrega en una cookie
 * HttpOnly; al cliente se le responde {@link #sinToken()} para que JavaScript nunca lo lea.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(String token, String email, String nombre, String apellido,
                            String tokenType, long expiresIn, UsuarioResponse usuario) {
    public LoginResponse(String token, String email, String nombre, String apellido) {
        this(token, email, nombre, apellido, "Bearer", 3600, null);
    }

    /** Copia para el body HTTP: sin token (viaja en la cookie HttpOnly). */
    public LoginResponse sinToken() {
        return new LoginResponse(null, email, nombre, apellido, "Cookie", expiresIn, usuario);
    }
}
