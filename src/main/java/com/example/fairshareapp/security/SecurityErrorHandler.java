package com.example.fairshareapp.security;
import com.example.fairshareapp.model.dto.ErrorResponseDTO;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;
@Component @RequiredArgsConstructor
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper mapper;
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        responder(request, response, 401, "NO_AUTENTICADO", "Debe enviar un token válido y vigente");
    }
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
        responder(request, response, 403, "ACCESO_DENEGADO", "No tiene permisos para esta operación");
    }
    private void responder(HttpServletRequest req, HttpServletResponse res, int status, String code, String message) throws IOException {
        res.setStatus(status); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8");
        res.getWriter().write(mapper.writeValueAsString(ErrorResponseDTO.builder().timestamp(Instant.now())
            .status(status).code(code).message(message).path(req.getRequestURI()).build()));
    }
}
