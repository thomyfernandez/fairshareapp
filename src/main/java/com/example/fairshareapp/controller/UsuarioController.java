package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.request.LoginRequest;
import com.example.fairshareapp.model.request.RegistroUsuarioRequest;
import com.example.fairshareapp.model.response.UsuarioResponse;
import com.example.fairshareapp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<String> Registro(@RequestBody RegistroUsuarioRequest request) {
        usuarioService.RegistrarUsuario(request);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/login")
    public ResponseEntity<String> Login(@RequestBody LoginRequest request) {
        usuarioService.LoginUsuario(request);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/get")
    public ResponseEntity<List<UsuarioResponse>> GetAllUsuarios() {
        List<UsuarioResponse> res = usuarioService.GetAllUsuarios();
        return ResponseEntity.ok(res);
    }
}
