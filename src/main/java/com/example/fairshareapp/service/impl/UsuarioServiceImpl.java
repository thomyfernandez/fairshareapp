package com.example.fairshareapp.service.impl;

import com.example.fairshareapp.exception.CredencialesInvalidasException;
import com.example.fairshareapp.exception.EmailYaRegistradoException;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.request.LoginRequest;
import com.example.fairshareapp.model.request.RegistroUsuarioRequest;
import com.example.fairshareapp.model.response.UsuarioResponse;
import com.example.fairshareapp.repository.UsuarioRepository;
import com.example.fairshareapp.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registrarUsuario(RegistroUsuarioRequest request) {
        if (usuarioRepository.getByEmail(request.email()).isPresent()) {
            throw new EmailYaRegistradoException(request.email());
        }

        Usuario newUsuario = Usuario.builder()
                .nombreUsuario(request.usuario())
                .email(request.email())
                .contra(passwordEncoder.encode(request.contra()))
                .nombre(request.nombre())
                .apellido(request.apellido())
                .build();

        usuarioRepository.save(newUsuario);
    }

    public void loginUsuario(LoginRequest request) {
        Usuario usuario = usuarioRepository.getByEmail(request.email())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(request.contra(), usuario.getContra())) {
            throw new CredencialesInvalidasException();
        }
    }

    public List<UsuarioResponse> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioResponse> usuarioResponses = new ArrayList<>();

        usuarios.forEach(
                usuario -> {
                    UsuarioResponse res = new UsuarioResponse(
                            usuario.getNombreUsuario(),
                            usuario.getEmail(),
                            usuario.getNombre(),
                            usuario.getApellido());
                    usuarioResponses.add(res);
                }
        );

        return usuarioResponses;
    }
}
