package com.example.fairshareapp.config;

import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.RolUsuario;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos de inicio acotado a entornos que no sean de produccion.
 * Permite contar con un usuario administrador para pruebas locales y desarrollo,
 * parametrizando las credenciales y previniendo su ejecucion en ambientes productivos.
 */
@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    /**
     * Constructor con inyeccion de dependencias y propiedades configurables.
     *
     * @param usuarioRepository Repositorio para operaciones sobre entidades de usuario.
     * @param passwordEncoder Codificador para almacenar contrasenas de forma segura.
     * @param adminEmail Correo electronico del administrador inicial configurable.
     * @param adminPassword Contrasena del administrador inicial configurable.
     */
    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email:admin@fairshare.com}") String adminEmail,
                           @Value("${app.admin.password:Admin123!}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    /**
     * Ejecuta la inicializacion asegurando la persistencia del usuario administrador inicial
     * unicamente en caso de que no exista previamente en la base de datos.
     *
     * @param args Argumentos recibidos por linea de comandos al iniciar la aplicacion.
     */
    @Override
    public void run(String... args) {
        // Verifica si la cuenta ya existe para garantizar idempotencia en reinicios sucesivos
        if (!usuarioRepository.existsByEmailIgnoreCase(adminEmail)) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .apellido("FairShare")
                    .email(adminEmail)
                    .nombreUsuario("admin")
                    .contra(passwordEncoder.encode(adminPassword))
                    .rol(RolUsuario.ADMIN)
                    .build();
            usuarioRepository.save(admin);
        }
    }
}
