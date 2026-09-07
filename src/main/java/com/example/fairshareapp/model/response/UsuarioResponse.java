package com.example.fairshareapp.model.response;

public record UsuarioResponse(
        String usuario,
        String email,
        String nombre,
        String apellido
) {
}
