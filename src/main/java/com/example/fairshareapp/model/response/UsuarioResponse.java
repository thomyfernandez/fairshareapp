package com.example.fairshareapp.model.response;
public record UsuarioResponse(Long id, String usuario, String email, String nombre, String apellido, String rol) {
    public UsuarioResponse(String usuario, String email, String nombre, String apellido) {
        this(null, usuario, email, nombre, apellido, "USUARIO");
    }
    public static UsuarioResponse de(com.example.fairshareapp.model.entity.Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombreUsuario(), u.getEmail(), u.getNombre(), u.getApellido(), u.getRol().name());
    }
}
