package com.example.fairshareapp.security;
import com.example.fairshareapp.model.entity.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
public final class UsuarioActual {
    private UsuarioActual() {}
    public static Usuario obtener() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario u ? u : null;
    }
    public static Usuario requerido() {
        Usuario u = obtener();
        if (u == null) throw new AuthenticationCredentialsNotFoundException("Debe iniciar sesión");
        return u;
    }
}
