package com.example.fairshareapp.service;

import com.example.fairshareapp.model.request.LoginRequest;
import com.example.fairshareapp.model.request.RegistroUsuarioRequest;
import com.example.fairshareapp.model.response.LoginResponse;
import com.example.fairshareapp.model.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse registrarUsuario(RegistroUsuarioRequest request);
    LoginResponse loginUsuario(LoginRequest request);
    List<UsuarioResponse> getAllUsuarios();
}
