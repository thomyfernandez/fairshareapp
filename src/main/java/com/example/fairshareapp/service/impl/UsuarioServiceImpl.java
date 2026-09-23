package com.example.fairshareapp.service.impl;
import com.example.fairshareapp.exception.*;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.request.*;
import com.example.fairshareapp.model.response.*;
import com.example.fairshareapp.repository.UsuarioRepository;
import com.example.fairshareapp.security.JwtService;
import com.example.fairshareapp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
@Service @Transactional @RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    public UsuarioResponse registrarUsuario(RegistroUsuarioRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) throw new EmailYaRegistradoException(email);
        if (request.contra().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ReglaInvalidaException("La contraseña no puede superar 72 bytes en UTF-8");
        Usuario usuario = Usuario.builder().nombreUsuario(request.usuario().strip()).email(email)
            .contra(passwordEncoder.encode(request.contra())).nombre(request.nombre().strip())
            .apellido(request.apellido().strip()).build();
        return UsuarioResponse.de(usuarioRepository.saveAndFlush(usuario));
    }
    public LoginResponse loginUsuario(LoginRequest request) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(request.email().strip())
            .orElseThrow(CredencialesInvalidasException::new);
        if (request.contra().getBytes(StandardCharsets.UTF_8).length > 72 || !passwordEncoder.matches(request.contra(), u.getContra()))
            throw new CredencialesInvalidasException();
        return new LoginResponse(jwtService.generarToken(u.getEmail()), u.getEmail(), u.getNombre(), u.getApellido(),
            "Bearer", jwtService.getExpirationMs() / 1000, UsuarioResponse.de(u));
    }
    @Transactional(readOnly=true)
    public List<UsuarioResponse> getAllUsuarios() { return usuarioRepository.findAll().stream().map(UsuarioResponse::de).toList(); }
}
