package com.example.fairshareapp.security;
import com.example.fairshareapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
@Service @RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    public UserDetails loadUserByUsername(String email) {
        return usuarios.findByEmailIgnoreCase(email.strip()).orElseThrow(() -> new UsernameNotFoundException("Usuario inexistente"));
    }
}
