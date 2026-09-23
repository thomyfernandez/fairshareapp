package com.example.fairshareapp.model.response;
public record LoginResponse(String token, String email, String nombre, String apellido,
                            String tokenType, long expiresIn, UsuarioResponse usuario) {
    public LoginResponse(String token, String email, String nombre, String apellido) {
        this(token, email, nombre, apellido, "Bearer", 3600, null);
    }
}
