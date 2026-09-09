package com.example.fairshareapp;

import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class FairshareappApplicationTests {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	/** Verifica que el nombre interno conserve la columna y la propiedad JSON existentes. */
	@Test
	@Transactional
	void nombreUsuario_conservaCompatibilidadConPersistenciaYJson() {
		Usuario usuario = usuarioRepository.saveAndFlush(Usuario.builder()
				.nombreUsuario("usuario_prueba")
				.email("usuario_prueba@example.com")
				.build());

		assertEquals("usuario_prueba", jdbcTemplate.queryForObject(
				"SELECT usuario FROM usuario WHERE id = ?", String.class, usuario.getId()));
		var json = JsonMapper.builder().build().valueToTree(usuario);
		assertEquals("usuario_prueba", json.get("usuario").asString());
		assertFalse(json.has("nombreUsuario"));
	}

}
