package com.example.fairshareapp.model.request;

public record RegistroUsuarioRequest(
        String nombre,
        String apellido,
        String email,
        String contra,
        String usuario
) {
}
