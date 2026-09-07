package com.example.fairshareapp.service;

import com.example.fairshareapp.model.request.LoginRequest;
import com.example.fairshareapp.model.request.RegistroUsuarioRequest;
import com.example.fairshareapp.model.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    void RegistrarUsuario(RegistroUsuarioRequest request);
    void LoginUsuario(LoginRequest request);
    List<UsuarioResponse> GetAllUsuarios();
}
