package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.CicloVencidoException;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarPrecioCicloDTO;
import com.example.fairshareapp.model.dto.CrearGastoDTO;
import com.example.fairshareapp.model.dto.GastoDetalleDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoRequestDTO;
import com.example.fairshareapp.model.dto.PlantillaGastoResponseDTO;
import com.example.fairshareapp.model.dto.ServicioVencimientoDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.MiembroEspacio;
import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import com.example.fairshareapp.model.enums.ReglaDivision;
import com.example.fairshareapp.model.enums.RolMiembro;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.MiembroEspacioRepository;
import com.example.fairshareapp.repository.PlantillaGastoRepository;
import com.example.fairshareapp.repository.ServicioRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para RecurrentesService verificando la administracion de plantillas,
 * deteccion de vencimientos de tarifas, pertenencia al espacio y ejecucion rapida en 1 clic.
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
    private MiembroEspacioRepository miembroEspacioRepository;

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
        org.mockito.Mockito.lenient().when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Thomas")
                .apellido("Fernandez")
                .email("thomas@example.com")
                .build();

        espacio = Espacio.builder().id(1L).nombre("Espacio Test").build();

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

    private MiembroEspacio miembro(Usuario u) {
        return MiembroEspacio.builder()
                .id(u.getId())
                .usuario(u)
                .espacio(espacio)
                .rol(RolMiembro.MIEMBRO)
                .build();
    }

    /**
     * Valida el registro correcto de una plantilla favorita en el espacio.
     */
    @Test
    void guardarFavorito_datosValidos_guardaYRetornaDTO() {
        PlantillaGastoRequestDTO dto = PlantillaGastoRequestDTO.builder()
                .nombre("Internet Fibra")
                .frecuenciaAjusteMeses(6)
                .montoBase(BigDecimal.valueOf(20000.00))
                .montoVariable(BigDecimal.ZERO)
                .fechaProximaRevision(LocalDate.now().plusMonths(6))
                .espacioId(1L)
                .pagadorId(1L)
                .build();

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(plantillaRepository.save(any(PlantillaGastoRecurrente.class))).thenAnswer(invocation -> {
            PlantillaGastoRecurrente entidad = invocation.getArgument(0);
            entidad.setId(20L);
            return entidad;
        });

        PlantillaGastoResponseDTO resultado = recurrentesService.guardarFavorito(dto);

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
        PlantillaGastoRequestDTO dto = PlantillaGastoRequestDTO.builder()
                .nombre("Luz")
                .espacioId(99L)
                .pagadorId(1L)
                .montoBase(BigDecimal.valueOf(15000))
                .fechaProximaRevision(LocalDate.now().plusMonths(1))
                .build();

        when(espacioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> recurrentesService.guardarFavorito(dto));
    }

    /**
     * Valida que un monto base negativo sea rechazado antes de tocar los repositorios.
     */
    @Test
    void guardarFavorito_montoBaseNegativo_lanzaReglaInvalida() {
        PlantillaGastoRequestDTO dto = PlantillaGastoRequestDTO.builder()
                .nombre("Gas")
                .espacioId(1L)
                .pagadorId(1L)
                .montoBase(BigDecimal.valueOf(-100))
                .fechaProximaRevision(LocalDate.now().plusMonths(1))
                .build();

        assertThrows(ReglaInvalidaException.class, () -> recurrentesService.guardarFavorito(dto));
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
     * Valida que eliminar una plantilla inexistente lance excepcion 404.
     */
    @Test
    void eliminarFavorito_idInexistente_lanzaExcepcion() {
        when(plantillaRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> recurrentesService.eliminarFavorito(99L));
    }

    /**
     * Valida que obtenerFavoritoPorId lance 404 para una plantilla inexistente.
     */
    @Test
    void obtenerFavoritoPorId_idInexistente_lanzaExcepcion() {
        when(plantillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> recurrentesService.obtenerFavoritoPorId(99L));
    }

    /**
     * Valida que el listado por espacio solo traiga las plantillas de ese espacio.
     */
    @Test
    void obtenerFavoritosPorEspacio_listaSoloLasDelEspacio() {
        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(plantillaRepository.findByEspacioId(1L)).thenReturn(List.of(plantilla));

        List<PlantillaGastoResponseDTO> favoritos = recurrentesService.obtenerFavoritosPorEspacio(1L, false);

        assertEquals(1, favoritos.size());
        assertEquals(1L, favoritos.get(0).getEspacioId());
        verify(plantillaRepository).findByEspacioId(1L);
        verify(plantillaRepository, never()).findVencidasPorEspacio(anyLong(), any());
    }

    /**
     * Valida que soloVencidos delegue el filtro en la query de vencidas por espacio.
     */
    @Test
    void obtenerFavoritosPorEspacio_soloVencidos_usaQueryDeVencidas() {
        plantilla.setFechaProximaRevision(LocalDate.now().minusDays(2));
        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(plantillaRepository.findVencidasPorEspacio(eq(1L), any(LocalDate.class))).thenReturn(List.of(plantilla));

        List<PlantillaGastoResponseDTO> favoritos = recurrentesService.obtenerFavoritosPorEspacio(1L, true);

        assertEquals(1, favoritos.size());
        assertTrue(favoritos.get(0).getVencido());
        verify(plantillaRepository, never()).findByEspacioId(anyLong());
    }

    /**
     * Valida la deteccion de vencimientos cuando la fecha de revision ya vencio o es hoy.
     */
    @Test
    void detectarVencimientos_conPlantillaVencida_retornaListaVencidos() {
        plantilla.setFechaProximaRevision(LocalDate.now().minusDays(5));

        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(plantillaRepository.findVencidasPorEspacio(eq(1L), any(LocalDate.class))).thenReturn(List.of(plantilla));

        List<ServicioVencimientoDTO> vencidos = recurrentesService.detectarVencimientos(1L);

        assertNotNull(vencidos);
        assertEquals(1, vencidos.size());
        assertEquals(10L, vencidos.get(0).getServicioId());
        assertEquals("Alquiler Departamento", vencidos.get(0).getNombreServicio());
        assertTrue(vencidos.get(0).getDiasVencido() >= 5);
    }

    /**
     * Valida que no se detecten vencimientos si no hay plantillas vencidas en el espacio.
     */
    @Test
    void detectarVencimientos_sinPlantillasVencidas_retornaListaVacia() {
        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(plantillaRepository.findVencidasPorEspacio(eq(1L), any(LocalDate.class))).thenReturn(List.of());

        List<ServicioVencimientoDTO> vencidos = recurrentesService.detectarVencimientos(1L);

        assertNotNull(vencidos);
        assertTrue(vencidos.isEmpty());
    }

    /**
     * Valida la actualizacion exitosa del monto base y variable, confirmando que renueva la fecha de revision.
     */
    @Test
    void actualizarMonto_datosValidos_actualizaMontosYFechaRevision() {
        LocalDate fechaOriginal = LocalDate.now().plusMonths(3);
        plantilla.setFechaProximaRevision(fechaOriginal);

        ActualizarPrecioCicloDTO dto = ActualizarPrecioCicloDTO.builder()
                .nuevoMontoBase(BigDecimal.valueOf(350000.00))
                .nuevoMontoVariable(BigDecimal.valueOf(30000.00))
                .build();

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));
        when(plantillaRepository.save(any(PlantillaGastoRecurrente.class))).thenAnswer(inv -> inv.getArgument(0));

        PlantillaGastoResponseDTO resultado = recurrentesService.actualizarMonto(10L, dto);

        assertNotNull(resultado);
        assertEquals(BigDecimal.valueOf(350000.00), resultado.getMontoBase());
        assertEquals(BigDecimal.valueOf(30000.00), resultado.getMontoVariable());
        assertEquals(fechaOriginal.plusMonths(3), resultado.getFechaProximaRevision());
        verify(plantillaRepository).save(plantilla);
    }

    /**
     * Valida que actualizarMonto sobre una plantilla inexistente devuelva 404.
     */
    @Test
    void actualizarMonto_idInexistente_lanzaExcepcion() {
        ActualizarPrecioCicloDTO dto = ActualizarPrecioCicloDTO.builder()
                .nuevoMontoBase(BigDecimal.valueOf(1000))
                .build();
        when(plantillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> recurrentesService.actualizarMonto(99L, dto));
    }

    /**
     * Valida que al intentar ejecutar una plantilla con ciclo vencido, se rechace con CicloVencidoException (409).
     */
    @Test
    void ejecutarGastoDesdePlantilla_cicloVencido_lanzaCicloVencidoException() {
        plantilla.setFechaProximaRevision(LocalDate.now().minusDays(1));

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));

        CicloVencidoException ex = assertThrows(CicloVencidoException.class,
                () -> recurrentesService.ejecutarGastoDesdePlantilla(10L));

        assertTrue(ex.getMessage().contains("Debe actualizar la tarifa antes de ejecutar"));
        verify(gastoService, never()).registrarGasto(anyLong(), any());
    }

    /**
     * Valida que ejecutar una plantilla inexistente devuelva 404 y no dispare la creacion de gasto.
     */
    @Test
    void ejecutarGastoDesdePlantilla_idInexistente_lanzaExcepcion() {
        when(plantillaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> recurrentesService.ejecutarGastoDesdePlantilla(99L));
        verify(gastoService, never()).registrarGasto(anyLong(), any());
    }

    /**
     * Valida que si el espacio no tiene miembros, la ejecucion se rechace antes de llamar a GastoService.
     */
    @Test
    void ejecutarGastoDesdePlantilla_espacioSinMiembros_lanzaReglaInvalida() {
        plantilla.setFechaProximaRevision(LocalDate.now().plusMonths(2));

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));
        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(List.of());

        assertThrows(ReglaInvalidaException.class, () -> recurrentesService.ejecutarGastoDesdePlantilla(10L));
        verify(gastoService, never()).registrarGasto(anyLong(), any());
    }

    /**
     * Valida que si el pagador configurado ya no es miembro del espacio, la ejecucion se rechace explicitamente
     * en lugar de caer silenciosamente a otro usuario.
     */
    @Test
    void ejecutarGastoDesdePlantilla_pagadorNoEsMiembro_lanzaReglaInvalida() {
        plantilla.setFechaProximaRevision(LocalDate.now().plusMonths(2));

        Usuario otroMiembro = Usuario.builder().id(2L).nombre("Ana").apellido("Lopez").email("ana@example.com").build();

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));
        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(List.of(miembro(otroMiembro)));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 1L)).thenReturn(false);

        assertThrows(ReglaInvalidaException.class, () -> recurrentesService.ejecutarGastoDesdePlantilla(10L));
        verify(gastoService, never()).registrarGasto(anyLong(), any());
    }

    /**
     * Valida que una plantilla vigente dispare la creacion de gasto en 1 clic a traves de GastoService,
     * usando exclusivamente los miembros del espacio (no todos los usuarios del sistema) como participantes.
     */
    @Test
    void ejecutarGastoDesdePlantilla_cicloVigente_usaMiembrosDelEspacioComoParticipantes() {
        plantilla.setFechaProximaRevision(LocalDate.now().plusMonths(2));

        Usuario otroMiembro = Usuario.builder().id(2L).nombre("Ana").apellido("Lopez").email("ana@example.com").build();

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));
        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(List.of(miembro(usuario), miembro(otroMiembro)));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 1L)).thenReturn(true);

        GastoDetalleDTO detalleMock = GastoDetalleDTO.builder()
                .id(100L)
                .descripcion("Alquiler Departamento")
                .monto(BigDecimal.valueOf(325000.0))
                .build();

        when(gastoService.registrarGasto(eq(1L), any(CrearGastoDTO.class))).thenReturn(detalleMock);

        GastoDetalleDTO resultado = recurrentesService.ejecutarGastoDesdePlantilla(10L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());

        ArgumentCaptor<CrearGastoDTO> captor = ArgumentCaptor.forClass(CrearGastoDTO.class);
        verify(gastoService).registrarGasto(eq(1L), captor.capture());
        CrearGastoDTO enviado = captor.getValue();
        assertEquals(1L, enviado.getPagadorId());
        assertEquals(2, enviado.getParticipantes().size());
        List<Long> idsParticipantes = enviado.getParticipantes().stream()
                .map(com.example.fairshareapp.model.dto.GastoParticipanteDTO::getUsuarioId)
                .toList();
        assertTrue(idsParticipantes.containsAll(List.of(1L, 2L)));
    }

    /**
     * Valida el flujo completo: ciclo vencido, se actualiza el monto/fecha y luego la ejecucion es exitosa.
     */
    @Test
    void ejecutarGastoDesdePlantilla_luegoDeActualizarCicloVencido_ejecutaCorrectamente() {
        plantilla.setFechaProximaRevision(LocalDate.now().minusDays(3));

        when(plantillaRepository.findById(10L)).thenReturn(Optional.of(plantilla));

        assertThrows(CicloVencidoException.class, () -> recurrentesService.ejecutarGastoDesdePlantilla(10L));

        ActualizarPrecioCicloDTO actualizacion = ActualizarPrecioCicloDTO.builder()
                .nuevoMontoBase(BigDecimal.valueOf(320000.00))
                .nuevaFechaProximaRevision(LocalDate.now().plusMonths(1))
                .build();
        when(plantillaRepository.save(any(PlantillaGastoRecurrente.class))).thenAnswer(inv -> inv.getArgument(0));

        recurrentesService.actualizarMonto(10L, actualizacion);
        assertFalse(plantilla.getFechaProximaRevision().isBefore(LocalDate.now()));

        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(List.of(miembro(usuario)));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 1L)).thenReturn(true);
        when(gastoService.registrarGasto(eq(1L), any(CrearGastoDTO.class)))
                .thenReturn(GastoDetalleDTO.builder().id(200L).descripcion("Alquiler Departamento").build());

        GastoDetalleDTO resultado = recurrentesService.ejecutarGastoDesdePlantilla(10L);

        assertNotNull(resultado);
        assertEquals(200L, resultado.getId());
    }
}
