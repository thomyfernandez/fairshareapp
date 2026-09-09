package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.dto.PresupuestoResponseDTO;
import com.example.fairshareapp.model.dto.PresupuestoUpsertDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Presupuesto;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.PresupuestoRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para verificar la logica de negocio de PresupuestoService:
 * la fijacion (creacion o actualizacion) del presupuesto mensual por usuario y su consulta.
 */
@ExtendWith(MockitoExtension.class)
class PresupuestoServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PresupuestoService presupuestoService;

    private Espacio espacio;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        espacio = Espacio.builder().id(1L).nombre("Depto Palermo").build();
        usuario = Usuario.builder().id(1L).nombre("Juan").apellido("Perez").build();
    }

    @Test
    void fijarPresupuesto_sinPresupuestoPrevio_creaUnoNuevo() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 1L, 2026, 9)).thenReturn(Optional.empty());
        when(presupuestoRepository.save(any(Presupuesto.class))).thenAnswer(inv -> {
            Presupuesto p = inv.getArgument(0);
            p.setId(5L);
            return p;
        });

        PresupuestoResponseDTO resultado = presupuestoService.fijarPresupuesto(1L, 1L, 2026, 9,
                PresupuestoUpsertDTO.builder().montoLimite(BigDecimal.valueOf(50000)).build());

        assertEquals(5L, resultado.getId());
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(resultado.getMontoLimite()));
    }

    @Test
    void fijarPresupuesto_conPresupuestoPrevio_actualizaMontoExistente() {
        Presupuesto existente = Presupuesto.builder().id(5L).espacio(espacio).usuario(usuario)
                .anio(2026).mes(9).montoLimite(BigDecimal.valueOf(40000)).build();

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(presupuestoRepository.findByEspacioIdAndUsuarioIdAndAnioAndMes(1L, 1L, 2026, 9)).thenReturn(Optional.of(existente));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenAnswer(inv -> inv.getArgument(0));

        PresupuestoResponseDTO resultado = presupuestoService.fijarPresupuesto(1L, 1L, 2026, 9,
                PresupuestoUpsertDTO.builder().montoLimite(BigDecimal.valueOf(60000)).build());

        assertEquals(5L, resultado.getId());
        assertEquals(0, BigDecimal.valueOf(60000).compareTo(resultado.getMontoLimite()));
    }

    @Test
    void fijarPresupuesto_espacioInexistente_lanzaExcepcion() {
        when(espacioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                presupuestoService.fijarPresupuesto(99L, 1L, 2026, 9,
                        PresupuestoUpsertDTO.builder().montoLimite(BigDecimal.valueOf(1000)).build()));
    }

    @Test
    void fijarPresupuesto_usuarioInexistente_lanzaExcepcion() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                presupuestoService.fijarPresupuesto(1L, 99L, 2026, 9,
                        PresupuestoUpsertDTO.builder().montoLimite(BigDecimal.valueOf(1000)).build()));
    }

    @Test
    void listarPresupuestos_espacioInexistente_lanzaExcepcion() {
        when(espacioRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> presupuestoService.listarPresupuestos(99L, null, null));
    }

    @Test
    void listarPresupuestos_conFiltroDeAnioYMes_retornaListaFiltrada() {
        Presupuesto presupuesto = Presupuesto.builder().id(5L).espacio(espacio).usuario(usuario)
                .anio(2026).mes(9).montoLimite(BigDecimal.valueOf(50000)).build();

        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(presupuestoRepository.findByEspacioIdAndAnioAndMes(1L, 2026, 9)).thenReturn(List.of(presupuesto));

        List<PresupuestoResponseDTO> resultado = presupuestoService.listarPresupuestos(1L, 2026, 9);

        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).getId());
    }
}
