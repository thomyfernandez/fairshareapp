package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.response.EstadoApiResponse;
import com.example.fairshareapp.model.response.MensajeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST con endpoints basicos de diagnostico del servicio.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private static final String NOMBRE_SERVICIO = "FairShare Backend API";

    /**
     * Retorna el estado del servicio y la marca de tiempo actual.
     *
     * @return ResponseEntity con el EstadoApiResponse y codigo HTTP 200 OK.
     */
    @GetMapping("/status")
    public ResponseEntity<EstadoApiResponse> getStatus() {
        EstadoApiResponse response = EstadoApiResponse.builder()
                .status("UP")
                .service(NOMBRE_SERVICIO)
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna un mensaje de confirmacion de conexion con el backend.
     *
     * @return ResponseEntity con el MensajeResponse y codigo HTTP 200 OK.
     */
    @GetMapping("/hello")
    public ResponseEntity<MensajeResponse> getHello() {
        return ResponseEntity.ok(new MensajeResponse("¡Conexión exitosa desde el Backend de Spring Boot!"));
    }
}
