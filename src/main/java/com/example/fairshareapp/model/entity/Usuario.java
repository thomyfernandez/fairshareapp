package com.example.fairshareapp.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Usuario implements org.springframework.security.core.userdetails.UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario")
    @JsonProperty("usuario")
    private String nombreUsuario;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false)
    private String contra;
    private String nombre;
    private String apellido;
    private BigDecimal sueldo;


    @Builder.Default
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'USUARIO'")
    private com.example.fairshareapp.model.enums.RolUsuario rol = com.example.fairshareapp.model.enums.RolUsuario.USUARIO;

    @Override @com.fasterxml.jackson.annotation.JsonIgnore
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol.name()));
    }
    @Override @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPassword() { return contra; }
    @Override @com.fasterxml.jackson.annotation.JsonIgnore
    public String getUsername() { return email; }
}
