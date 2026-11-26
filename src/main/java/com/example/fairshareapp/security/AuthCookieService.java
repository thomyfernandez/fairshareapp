package com.example.fairshareapp.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Crea, borra y lee la cookie de sesion que transporta el JWT.
 * <ul>
 *   <li>HttpOnly: JavaScript no puede leerla (mitiga robo de token por XSS).</li>
 *   <li>SameSite=Lax: el navegador no la envia en POST/PUT/DELETE originados en otros sitios (mitiga CSRF).</li>
 *   <li>Secure: configurable; debe ser true en produccion (solo HTTPS).</li>
 * </ul>
 */
@Component
public class AuthCookieService {

    private final String nombre;
    private final boolean secure;
    private final String sameSite;
    private final Duration duracion;

    public AuthCookieService(@Value("${app.auth.cookie-name:fairshare_token}") String nombre,
                             @Value("${app.auth.cookie-secure:false}") boolean secure,
                             @Value("${app.auth.cookie-same-site:Lax}") String sameSite,
                             @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.nombre = nombre;
        this.secure = secure;
        this.sameSite = sameSite;
        this.duracion = Duration.ofMillis(expirationMs);
    }

    public ResponseCookie crear(String token) {
        return base(token).maxAge(duracion).build();
    }

    public ResponseCookie borrar() {
        return base("").maxAge(Duration.ZERO).build();
    }

    public Optional<String> leer(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> nombre.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(valor -> !valor.isBlank())
                .findFirst();
    }

    public String getNombre() {
        return nombre;
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(nombre, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/");
    }
}
