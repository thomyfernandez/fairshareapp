package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarPrecioCicloDTO;
import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import com.example.fairshareapp.model.enums.ReglaDivision;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.PlantillaGastoRepository;
import com.example.fairshareapp.repository.ServicioRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para RecurrentesService verificando la administracion de plantillas,
 * deteccion de vencimientos de tarifas y ejecucion rapida en 1 clic.
 */
@ExtendWith(MockitoExtension.class)
class RecurrentesServiceTest {

    @Mock
    private PlantillaGastoRepository plantillaRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GastoService gastoService;

    @InjectMocks
    private RecurrentesService recurrentesService;

    private PlantillaGastoRecurrente plantilla;
    private Usuario usuario;
    private Espacio espacio;

    /**
     * Configuracion inicial previa a cada caso de prueba.
     */
    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Thomas")
                .apellido("Fernandez")
                .email("thomas@example.com")
                .build();

        espacio = Espacio.builder().id(1L).build();

        plantilla = PlantillaGastoRecurrente.builder()
                .id(10L)
                .nombre("Alquiler Departamento")
                .frecuenciaAjusteMeses(3)
                .montoBase(BigDecimal.valueOf(300000.00))
                .montoVariable(BigDecimal.valueOf(25000.00))
                .fechaProximaRevision(LocalDate.now().plusMonths(3))
                .espacioId(espacio)
                .pagadorId(usuario)
                .reglaDivision(ReglaDivision.EQUITATIVA)
                .build();
    }

    /**
     * Valida el registro correcto de una plantilla favorita en el espacio.
     */
    @Test
    void guardarFavorito_datosValidos_guardaYRetornaDTO() {
        PlantillaGastoDTO dto = PlantillaGastoDTO.builder()
                .nombre("Internet Fibra")
                .frecuenciaAjusteMeses(6)
                .montoBase(BigDecimal.valueOf(20000.00))
                .montoVariable(BigDecimal.ZERO)
                .fechaProximaRevision(LocalDate.now().plusMonths(6))
                .espacioId(1L)
                .build();

        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(plantillaRepository.save(any(PlantillaGastoRecurrente.class))).thenAnswer(invocation -> {
            PlantillaGastoRecurrente entidad = invocation.getArgument(0);
            entidad.setId(20L);
            return entidad;
        });

        PlantillaGastoDTO resultado = recurrentesService.guardarFavorito(dto);

        assertNotNull(resultado);
        assertEquals(20L, resultado.getId());
        assertEquals("Internet Fibra", resultado.getNombre());
        assertFalse(resultado.getVencido());
        verify(plantillaRepository).save(any(PlantillaGastoRecurrente.class));
    }

    /**
     * Valida que si el espacio no existe se lance excepcion de recurso no encontrado.
     */
    @Test
    void guardarFavorito_espacioNoExiste_lanzaExcepcion() {
        PlantillaGastoDTO dto = PlantillaGastoDTO.builder()
                .nombre("Luz")
                .espacioId(99L)
                .montoBase(BigDecimal.valueOf(15000))
                .build();

        when(espacioRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> recurrentesService.guardarFavorito(dto));
    }

    /**
     * Valida la eliminacion satisfactoria de una plantilla existente.
     */
    @Test
    void eliminarFavorito_idValido_eliminaCorrectamente() {
        when(plantillaRepository.existsById(10L)).thenReturn(true);

        recurrentesService.eliminarFavorito(10L);

        verify(plantillaRepository).deleteById(10L);
    }

    /**
     * Valida que eliminar una plantilla inexistente lance excepcion.
     */
    @Test
    void eliminarFavorito_idInexistente_lanzaExcepcion() {
        when(plantillaRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> recurrentesService.eliminarFavorito(99L));
    }

    /**
     * Valida la deteccion de vencimientos cuando la fecha de revision ya vencio o es hoy.
     */
    @Test
    void detectarVencimientos_conPlantillaVencida_retornaListaVencidos() {
        plantilla.setFechaProximaRevision(LocalDate.now().minusDays(5));

        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(plantillaRepository.findByEspacioId(1L)).thenReturn(List.of(plantilla));

        List<ServicioVencimientoDTO> vencidos = recurrentesService.detectarVencimientos(1L);

        assertNotNull(vencidos);
        assertEquals(1, vencidos.size());
        assertEquals(10L, vencidos.get(0).getServicioId());
        assertEquals("Alquiler Departamento", vencidos.get(0).getNombreServicio());
        assertTrue(vencidos.get(0).getDiasVencido() >= 5);
    }

    /**
     * Valida que no se detecten vencimientos si la fecha de revision es posterior a la actual.
     */
    @Test
    void detectarVencimientos_conPlantillaVigente_retornaListaVacia() {
        plantilla.setFechaProximaRevision(LocalDate.now().plusDays(10));

        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(plantillaRepository.findByEspacioId(1L)).thenReturn(List.of(plantilla));

        List<ServicioVencimientoDTO> vencidos = recurrentesService.detectarVencimientos(1L);

        assertNotNull(vencidos);
        assertTrue(vencidos.isEmpty());
    }

    /**
     * Valida la actualizacion exitosa del monto base y variable del ciclo.
     */
    @Test
    void actualizarMonto_datosValidos_actualizaMontosYFechaRevision() {
        ActualizarPrecioCicloDTO dto = ActualizarPrecioCicloDTO.builder()
                .nuevoMontoBase(BigDecimal.valueOf(350000.00))
                .nuevoMontoVariable(BigDecimal.valueOf(30000.00))
                .build();

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));
        when(plantillaRepository.save(any(PlantillaGastoRecurrente.class))).thenAnswer(inv -> inv.getArgument(0));

        PlantillaGastoDTO resultado = recurrentesService.actualizarMonto(10L, dto);

        assertNotNull(resultado);
        assertEquals(BigDecimal.valueOf(350000.00), resultado.getMontoBase());
        assertEquals(BigDecimal.valueOf(30000.00), resultado.getMontoVariable());
        verify(plantillaRepository).save(plantilla);
    }

    /**
     * Valida que al intentar ejecutar una plantilla con ciclo vencido, se rechace y exija actualizacion.
     */
    @Test
    void ejecutarGastoDesdePlantilla_cicloVencido_lanzaReglaInvalidaExigiendoActualizacion() {
        plantilla.setFechaProximaRevision(LocalDate.now().minusDays(1));

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));

        ReglaInvalidaException ex = assertThrows(ReglaInvalidaException.class,
                () -> recurrentesService.ejecutarGastoDesdePlantilla(10L));

        assertTrue(ex.getMessage().contains("Debe actualizar la tarifa antes de ejecutar"));
    }

    /**
     * Valida que una plantilla vigente dispare la creacion de gasto en 1 clic a traves de GastoService.
     */
    @Test
    void ejecutarGastoDesdePlantilla_cicloVigente_disparaGastoExitosamente() {
        plantilla.setFechaProximaRevision(LocalDate.now().plusMonths(2));

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        GastoDetalleDTO detalleMock = GastoDetalleDTO.builder()
                .id(100L)
                .descripcion("Alquiler Departamento")
                .monto(BigDecimal.valueOf(325000.0))
                .build();

        when(gastoService.registrarGasto(eq(1L), any(CrearGastoDTO.class))).thenReturn(detalleMock);

        GastoDetalleDTO resultado = recurrentesService.ejecutarGastoDesdePlantilla(10L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals("Alquiler Departamento", resultado.getDescripcion());
        verify(gastoService).registrarGasto(eq(1L), any(CrearGastoDTO.class));
    }
}
