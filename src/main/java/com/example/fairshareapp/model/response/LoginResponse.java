package com.example.fairshareapp.model.response;

public record LoginResponse(
        String token,
        String email,
        String nombre,
        String apellido
) {
}
