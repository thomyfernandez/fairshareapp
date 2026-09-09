package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.GastoParticipanteDTO;
import com.example.fairshareapp.model.entity.Categoria;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.Gasto;
import com.example.fairshareapp.model.enums.ReglaDivision;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.CategoriaRepository;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.GastoRepository;
import com.example.fairshareapp.repository.SueldoRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para verificar la logica de negocio de GastoService,
 * especialmente el calculo de importes de cada regla de division activa.
 */
@ExtendWith(MockitoExtension.class)
class GastoServiceTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private SueldoRepository sueldoRepository;

    @InjectMocks
    private GastoService gastoService;

    private Espacio espacio;
    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuario3;
    private Categoria categoria;

    /**
     * Configuracion inicial previa a cada prueba.
     */
    @BeforeEach
    void setUp() {
        espacio = Espacio.builder().id(1L).nombre("Departamento Centro").build();

        usuario1 = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .email("juan@test.com")
                .sueldo(BigDecimal.valueOf(200000.0))
                .build();

        usuario2 = Usuario.builder()
                .id(2L)
                .nombre("Maria")
                .apellido("Gomez")
                .email("maria@test.com")
                .sueldo(BigDecimal.valueOf(100000.0))
                .build();

        usuario3 = Usuario.builder()
                .id(3L)
                .nombre("Lucas")
                .apellido("Diaz")
                .email("lucas@test.com")
                .sueldo(BigDecimal.valueOf(100000.0))
                .build();

        categoria = Categoria.builder().id(1L).nombre("Supermercado").build();
    }

    /**
     * Prueba el calculo de la regla de division Equitativa ajustando diferencias residuales.
     */
    @Test
    void registrarGasto_ReglaEquitativa_CalculaCuotasIguales() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario3));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> {
            Gasto g = invocation.getArgument(0);
            g.setId(10L);
            return g;
        });

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Cena compartida")
                .monto(BigDecimal.valueOf(100.0))
                .pagadorId(1L)
                .categoriaId(1L)
                .regla(ReglaDivision.EQUITATIVA)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).build(),
                        GastoParticipanteDTO.builder().usuarioId(3L).build()
                ))
                .build();

        GastoDetalleDTO resultado = gastoService.registrarGasto(1L, dto);

        assertNotNull(resultado);
        assertEquals(3, resultado.getParticipantes().size());
        // El primer participante absorbe el ajuste de centavo (33.34 + 33.33 + 33.33 = 100.00)
        assertEquals(33.34, resultado.getParticipantes().get(0).getImporte().doubleValue(), 0.01);
        assertEquals(33.33, resultado.getParticipantes().get(1).getImporte().doubleValue(), 0.01);
        assertEquals(33.33, resultado.getParticipantes().get(2).getImporte().doubleValue(), 0.01);
    }

    /**
     * Prueba la regla de division Proporcional por Ingresos segun los sueldos declarados.
     */
    @Test
    void registrarGasto_ReglaProporcionalIngresos_CalculaSegunSueldo() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1)); // sueldo 200k (66.67%)
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2)); // sueldo 100k (33.33%)
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> {
            Gasto g = invocation.getArgument(0);
            g.setId(20L);
            return g;
        });

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Alquiler mensual")
                .monto(BigDecimal.valueOf(30000.0))
                .pagadorId(1L)
                .regla(ReglaDivision.PROPORCIONAL_INGRESOS)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).build()
                ))
                .build();

        GastoDetalleDTO resultado = gastoService.registrarGasto(1L, dto);

        assertNotNull(resultado);
        assertEquals(2, resultado.getParticipantes().size());
        assertEquals(20000.0, resultado.getParticipantes().get(0).getImporte().doubleValue(), 0.01);
        assertEquals(10000.0, resultado.getParticipantes().get(1).getImporte().doubleValue(), 0.01);
    }

    /**
     * Valida que la regla de division proporcional de ingresos utilice los sueldos
     * registrados especificamente para el mes y anio en que se efectua el gasto.
     */
    @Test
    void registrarGasto_ReglaProporcionalIngresos_UtilizaSueldoDelPeriodoCorrespondiente() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> {
            Gasto g = invocation.getArgument(0);
            g.setId(25L);
            return g;
        });

        // Simula sueldos especificos para septiembre 2026: usuario1 = 300k (75%), usuario2 = 100k (25%)
        com.example.fairshareapp.model.entity.Sueldo sueldoU1 = com.example.fairshareapp.model.entity.Sueldo.builder()
                .monto(BigDecimal.valueOf(300000.0))
                .mes(9)
                .anio(2026)
                .build();
        com.example.fairshareapp.model.entity.Sueldo sueldoU2 = com.example.fairshareapp.model.entity.Sueldo.builder()
                .monto(BigDecimal.valueOf(100000.0))
                .mes(9)
                .anio(2026)
                .build();

        when(sueldoRepository.findByUsuario_IdAndAnioAndMes(1L, 2026, 9)).thenReturn(Optional.of(sueldoU1));
        when(sueldoRepository.findByUsuario_IdAndAnioAndMes(2L, 2026, 9)).thenReturn(Optional.of(sueldoU2));

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Expensas Septiembre")
                .monto(BigDecimal.valueOf(40000.0))
                .fecha(java.time.LocalDate.of(2026, 9, 5))
                .pagadorId(1L)
                .regla(ReglaDivision.PROPORCIONAL_INGRESOS)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).build()
                ))
                .build();

        GastoDetalleDTO resultado = gastoService.registrarGasto(1L, dto);

        assertNotNull(resultado);
        assertEquals(2, resultado.getParticipantes().size());
        assertEquals(30000.0, resultado.getParticipantes().get(0).getImporte().doubleValue(), 0.01);
        assertEquals(10000.0, resultado.getParticipantes().get(1).getImporte().doubleValue(), 0.01);
    }

    /**
     * Prueba la regla de Participacion Parcial dividiendo solo entre los miembros seleccionados.
     */
    @Test
    void registrarGasto_ReglaParticipacionParcial_DivideEntreSeleccionados() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Taxi Juan y Maria")
                .monto(BigDecimal.valueOf(5000.0))
                .pagadorId(1L)
                .regla(ReglaDivision.PARTICIPACION_PARCIAL)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).build()
                ))
                .build();

        GastoDetalleDTO resultado = gastoService.registrarGasto(1L, dto);

        assertNotNull(resultado);
        assertEquals(2, resultado.getParticipantes().size());
        assertEquals(2500.0, resultado.getParticipantes().get(0).getImporte().doubleValue(), 0.01);
        assertEquals(2500.0, resultado.getParticipantes().get(1).getImporte().doubleValue(), 0.01);
    }

    /**
     * Prueba la regla Personalizada con porcentajes que suman 100%.
     */
    @Test
    void registrarGasto_ReglaPersonalizadaPorcentajes_CalculaCorrectamente() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Servicio streaming")
                .monto(BigDecimal.valueOf(1000.0))
                .pagadorId(1L)
                .regla(ReglaDivision.PERSONALIZADA)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).porcentaje(BigDecimal.valueOf(70.0)).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).porcentaje(BigDecimal.valueOf(30.0)).build()
                ))
                .build();

        GastoDetalleDTO resultado = gastoService.registrarGasto(1L, dto);

        assertNotNull(resultado);
        assertEquals(700.0, resultado.getParticipantes().get(0).getImporte().doubleValue(), 0.01);
        assertEquals(300.0, resultado.getParticipantes().get(1).getImporte().doubleValue(), 0.01);
    }

    /**
     * Prueba que la regla Personalizada lance excepcion si los porcentajes no suman 100%.
     */
    @Test
    void registrarGasto_ReglaPersonalizadaPorcentajesInvalidos_LanzaExcepcion() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Servicio streaming")
                .monto(BigDecimal.valueOf(1000.0))
                .pagadorId(1L)
                .regla(ReglaDivision.PERSONALIZADA)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).porcentaje(BigDecimal.valueOf(50.0)).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).porcentaje(BigDecimal.valueOf(30.0)).build()
                ))
                .build();

        assertThrows(ReglaInvalidaException.class, () -> gastoService.registrarGasto(1L, dto));
    }

    /**
     * Prueba la regla Personalizada con importes fijos que suman exactamente el monto total.
     */
    @Test
    void registrarGasto_ReglaPersonalizadaImportes_CalculaCorrectamente() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Compra personalizada")
                .monto(BigDecimal.valueOf(1000.0))
                .pagadorId(1L)
                .regla(ReglaDivision.PERSONALIZADA)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).importe(BigDecimal.valueOf(600.0)).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).importe(BigDecimal.valueOf(400.0)).build()
                ))
                .build();

        GastoDetalleDTO resultado = gastoService.registrarGasto(1L, dto);

        assertNotNull(resultado);
        assertEquals(600.0, resultado.getParticipantes().get(0).getImporte().doubleValue(), 0.01);
        assertEquals(400.0, resultado.getParticipantes().get(1).getImporte().doubleValue(), 0.01);
    }

    /**
     * Prueba que la regla Personalizada lance excepcion si la suma de importes no coincide con el total.
     */
    @Test
    void registrarGasto_ReglaPersonalizadaImportesInvalidos_LanzaExcepcion() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Compra personalizada")
                .monto(BigDecimal.valueOf(1000.0))
                .pagadorId(1L)
                .regla(ReglaDivision.PERSONALIZADA)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).importe(BigDecimal.valueOf(500.0)).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).importe(BigDecimal.valueOf(400.0)).build()
                ))
                .build();

        assertThrows(ReglaInvalidaException.class, () -> gastoService.registrarGasto(1L, dto));
    }

    /**
     * Prueba que la regla Personalizada lance excepcion si no se indican porcentajes ni importes.
     */
    @Test
    void registrarGasto_ReglaPersonalizadaSinDatos_LanzaExcepcion() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));

        CrearGastoDTO dto = CrearGastoDTO.builder()
                .descripcion("Compra personalizada")
                .monto(BigDecimal.valueOf(1000.0))
                .pagadorId(1L)
                .regla(ReglaDivision.PERSONALIZADA)
                .participantes(Arrays.asList(
                        GastoParticipanteDTO.builder().usuarioId(1L).build(),
                        GastoParticipanteDTO.builder().usuarioId(2L).build()
                ))
                .build();

        assertThrows(ReglaInvalidaException.class, () -> gastoService.registrarGasto(1L, dto));
    }

    /**
     * Prueba la consulta de lote de gastos filtrando por rango de fechas.
     */
    @Test
    void obtenerGastosPorEspacioYPeriodo_ConFechas_RetornaGastosFiltrados() {
        when(espacioRepository.existsById(1L)).thenReturn(true);

        LocalDate desde = LocalDate.of(2026, 9, 1);
        LocalDate hasta = LocalDate.of(2026, 9, 30);

        Gasto gasto1 = Gasto.builder()
                .id(1L)
                .descripcion("Gasto periodo")
                .monto(BigDecimal.valueOf(1500.0))
                .fecha(LocalDate.of(2026, 9, 5))
                .espacio(espacio)
                .pagador(usuario1)
                .reglaDivision(ReglaDivision.EQUITATIVA)
                .build();

        when(gastoRepository.findByEspacioIdAndFechaBetweenOrderByFechaDesc(1L, desde, hasta))
                .thenReturn(List.of(gasto1));

        List<GastoDetalleDTO> resultado = gastoService.obtenerGastosPorEspacioYPeriodo(1L, desde, hasta);

        assertEquals(1, resultado.size());
        assertEquals("Gasto periodo", resultado.get(0).getDescripcion());
    }

    /**
     * Prueba la eliminacion de un gasto existente y verificacion de interaccion con repositorio.
     */
    @Test
    void eliminarGasto_GastoExistente_InvocaDeleteById() {
        when(gastoRepository.existsById(10L)).thenReturn(true);

        gastoService.eliminarGasto(10L);

        verify(gastoRepository).deleteById(10L);
    }

    /**
     * Prueba el intento de eliminacion de un gasto inexistente arrojando RecursoNoEncontradoException.
     */
    @Test
    void eliminarGasto_Inexistente_LanzaRecursoNoEncontradoException() {
        when(gastoRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> gastoService.eliminarGasto(999L));
    }
}
