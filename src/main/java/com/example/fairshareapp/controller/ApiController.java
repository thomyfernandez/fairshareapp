package com.example.fairshareapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "FairShare Backend API");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/hello")
    public Map<String, String> getHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "¡Conexión exitosa desde el Backend de Spring Boot!");
        return response;
    }
}
