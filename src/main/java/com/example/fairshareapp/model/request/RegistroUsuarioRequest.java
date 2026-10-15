package com.example.fairshareapp.model.request;
import jakarta.validation.constraints.*;
public record RegistroUsuarioRequest(
    @NotBlank @Size(max=100) String nombre,
    @NotBlank @Size(max=100) String apellido,
    @NotBlank @Email @Size(max=254) String email,
    @NotBlank @Size(min=8, max=72) String contra,
    @NotBlank @Size(min=2, max=50) String usuario
) {}
