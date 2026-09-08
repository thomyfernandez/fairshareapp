package com.example.fairshareapp.model.dto;

import com.example.fairshareapp.model.enums.RolMiembro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta que expone la informacion de la pertenencia de un usuario a un espacio compartido.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiembroResponseDTO {

    private Long id;
    private Long espacioId;
    private Long usuarioId;
    private String nombreUsuario;
    private String emailUsuario;
    private RolMiembro rol;
    private BigDecimal sueldoDeclarado;
}
