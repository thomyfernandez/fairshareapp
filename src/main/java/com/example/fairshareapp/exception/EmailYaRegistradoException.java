package com.example.fairshareapp.exception;

public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
}
