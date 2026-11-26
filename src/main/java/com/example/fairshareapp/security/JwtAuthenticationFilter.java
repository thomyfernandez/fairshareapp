package com.example.fairshareapp.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.jsonwebtoken.JwtException;

/**
 * Autentica cada solicitud con el JWT recibido en:
 * <ul>
 *   <li>el header {@code Authorization: Bearer} (Postman, tests, clientes no navegador): si es
 *       invalido se responde 401;</li>
 *   <li>o la cookie HttpOnly de sesion (navegador): si esta vencida o es invalida se ignora y la
 *       solicitud sigue como anonima, para que una cookie vieja no impida volver a iniciar sesion.</li>
 * </ul>
 */
@Component @RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioDetailsService usuarios;
    private final SecurityErrorHandler errors;
    private final AuthCookieService authCookieService;
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.startsWith("Bearer ")) throw new BadCredentialsException("Bearer requerido");
                autenticar(header.substring(7));
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException | BadCredentialsException ex) {
                SecurityContextHolder.clearContext();
                errors.commence(request, response, new BadCredentialsException("Token inválido")); return;
            }
        } else {
            authCookieService.leer(request).ifPresent(token -> {
                try {
                    autenticar(token);
                } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                    SecurityContextHolder.clearContext();
                }
            });
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token) {
        var usuario = usuarios.loadUserByUsername(jwtService.extraerEmail(token));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }
}
