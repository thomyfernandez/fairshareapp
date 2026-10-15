package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.exception.SueldoDuplicadoException;
import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;
import com.example.fairshareapp.model.mapper.SueldoMapper;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.model.response.SueldoUpsertResult;
import com.example.fairshareapp.repository.SueldoRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import com.example.fairshareapp.service.impl.SueldoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para validar la logica de negocio de SueldoService
 * y su integracion con la entidad Usuario para la division proporcional de gastos.
 */
@ExtendWith(MockitoExtension.class)
class SueldoServiceTest {

    @Mock
    private SueldoRepository sueldoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private SueldoMapper sueldoMapper = new SueldoMapper();

    @InjectMocks
    private SueldoServiceImpl sueldoService;

    private Usuario usuario;
    private Sueldo sueldo;

    /**
     * Inicializa datos de prueba comunes para cada ejecucion.
     */
    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Martin")
                .email("martin@test.com")
                .sueldo(BigDecimal.ZERO)
                .build();

        sueldo = Sueldo.builder()
                .id(10L)
                .monto(BigDecimal.valueOf(800000.00))
                .tipo(TipoSueldo.FIJO)
                .frecuencia(FrecuenciaSueldo.MENSUAL)
                .mes(9)
                .anio(2026)
                .usuario(usuario)
                .build();
    }

    /**
     * Valida la creacion de un sueldo para un periodo nuevo, marcando el resultado como creado
     * y sincronizando el sueldo vigente del usuario.
     */
    @Test
    void crearOActualizarSueldo_PeriodoNuevo_CreaSueldoYSincronizaUsuario() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(850000.00));
        request.setTipo(TipoSueldo.FIJO);
        request.setFrecuencia(FrecuenciaSueldo.MENSUAL);
        request.setMes(9);
        request.setAnio(2026);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(sueldoRepository.findByUsuario_IdAndAnioAndMes(1L, 2026, 9)).thenReturn(Optional.empty());
        when(sueldoRepository.save(any(Sueldo.class))).thenAnswer(i -> {
            Sueldo s = i.getArgument(0);
            s.setId(11L);
            return s;
        });
        when(sueldoRepository.findFirstByUsuario_IdOrderByAnioDescMesDesc(1L)).thenReturn(Optional.of(sueldo));

        SueldoUpsertResult resultado = sueldoService.crearOActualizarSueldo(1L, request);

        assertTrue(resultado.creado());
        assertNotNull(resultado.sueldo());
        assertEquals(BigDecimal.valueOf(850000.00), resultado.sueldo().getMonto());
        verify(usuarioRepository).save(usuario);
        verify(sueldoRepository).save(any(Sueldo.class));
    }

    /**
     * Valida que si ya existe un sueldo para el mismo mes y anio, se actualiza el registro
     * existente (upsert) sin crear uno nuevo.
     */
    @Test
    void crearOActualizarSueldo_MismoPeriodoExiste_ActualizaRegistroExistente() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(950000.00));
        request.setTipo(TipoSueldo.VARIABLE);
        request.setFrecuencia(FrecuenciaSueldo.MENSUAL);
        request.setMes(9);
        request.setAnio(2026);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(sueldoRepository.findByUsuario_IdAndAnioAndMes(1L, 2026, 9)).thenReturn(Optional.of(sueldo));
        when(sueldoRepository.save(any(Sueldo.class))).thenAnswer(i -> i.getArgument(0));
        when(sueldoRepository.findFirstByUsuario_IdOrderByAnioDescMesDesc(1L)).thenReturn(Optional.of(sueldo));

        SueldoUpsertResult resultado = sueldoService.crearOActualizarSueldo(1L, request);

        assertFalse(resultado.creado());
        assertEquals(10L, resultado.sueldo().getId());
        assertEquals(BigDecimal.valueOf(950000.00), resultado.sueldo().getMonto());
        assertEquals(TipoSueldo.VARIABLE, resultado.sueldo().getTipo());
    }

    /**
     * Valida que se lance RecursoNoEncontradoException cuando el usuario no existe.
     */
    @Test
    void crearOActualizarSueldo_UsuarioInexistente_LanzaRecursoNoEncontradoException() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(500000.00));

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sueldoService.crearOActualizarSueldo(99L, request));
    }

    /**
     * Valida que se rechace un mes fuera del rango 1-12.
     */
    @Test
    void crearOActualizarSueldo_MesInvalido_LanzaReglaInvalidaException() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(500000.00));
        request.setTipo(TipoSueldo.FIJO);
        request.setFrecuencia(FrecuenciaSueldo.MENSUAL);
        request.setMes(13);
        request.setAnio(2026);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(ReglaInvalidaException.class, () -> sueldoService.crearOActualizarSueldo(1L, request));
        verify(sueldoRepository, never()).save(any());
    }

    /**
     * Valida la obtencion de un sueldo por su identificador.
     */
    @Test
    void obtenerSueldo_Existente_RetornaResponse() {
        when(sueldoRepository.findById(10L)).thenReturn(Optional.of(sueldo));

        SueldoResponse response = sueldoService.obtenerSueldo(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(BigDecimal.valueOf(800000.00), response.getMonto());
    }

    /**
     * Valida que consultar un sueldo inexistente lance RecursoNoEncontradoException.
     */
    @Test
    void obtenerSueldo_Inexistente_LanzaRecursoNoEncontradoException() {
        when(sueldoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sueldoService.obtenerSueldo(999L));
    }

    /**
     * Valida la actualizacion de los valores de un sueldo sin cambiar su periodo.
     */
    @Test
    void actualizarSueldo_Existente_ActualizaValores() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(900000.00));
        request.setTipo(TipoSueldo.VARIABLE);
        request.setFrecuencia(FrecuenciaSueldo.QUINCENAL);

        when(sueldoRepository.findById(10L)).thenReturn(Optional.of(sueldo));
        when(sueldoRepository.save(any(Sueldo.class))).thenAnswer(i -> i.getArgument(0));
        when(sueldoRepository.findFirstByUsuario_IdOrderByAnioDescMesDesc(1L)).thenReturn(Optional.of(sueldo));

        SueldoResponse response = sueldoService.actualizarSueldo(10L, request);

        assertEquals(BigDecimal.valueOf(900000.00), response.getMonto());
        assertEquals(TipoSueldo.VARIABLE, response.getTipo());
        assertEquals(FrecuenciaSueldo.QUINCENAL, response.getFrecuencia());
        verify(sueldoRepository).save(sueldo);
    }

    /**
     * Valida que actualizar el periodo de un sueldo a uno ya ocupado por otro registro del
     * mismo usuario lance SueldoDuplicadoException.
     */
    @Test
    void actualizarSueldo_PeriodoDuplicado_LanzaSueldoDuplicadoException() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(900000.00));
        request.setTipo(TipoSueldo.FIJO);
        request.setFrecuencia(FrecuenciaSueldo.MENSUAL);
        request.setMes(8);
        request.setAnio(2026);

        Sueldo otroSueldoMismoPeriodo = Sueldo.builder().id(20L).usuario(usuario).mes(8).anio(2026).build();

        when(sueldoRepository.findById(10L)).thenReturn(Optional.of(sueldo));
        when(sueldoRepository.findByUsuario_IdAndAnioAndMes(1L, 2026, 8))
                .thenReturn(Optional.of(otroSueldoMismoPeriodo));

        assertThrows(SueldoDuplicadoException.class, () -> sueldoService.actualizarSueldo(10L, request));
        verify(sueldoRepository, never()).save(any());
    }

    /**
     * Valida que actualizar un sueldo inexistente lance RecursoNoEncontradoException.
     */
    @Test
    void actualizarSueldo_Inexistente_LanzaRecursoNoEncontradoException() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(900000.00));

        when(sueldoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sueldoService.actualizarSueldo(999L, request));
    }

    /**
     * Valida el listado completo de sueldos disponibles.
     */
    @Test
    void obtenerTodos_RetornaLista() {
        when(sueldoRepository.findAll()).thenReturn(List.of(sueldo));

        List<SueldoResponse> lista = sueldoService.obtenerTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(10L, lista.get(0).getId());
    }

    /**
     * Valida que la consulta por usuario no mezcle registros de otros usuarios.
     */
    @Test
    void obtenerSueldosPorUsuario_RetornaSoloDelUsuario() {
        when(sueldoRepository.findByUsuario_Id(1L)).thenReturn(List.of(sueldo));

        List<SueldoResponse> lista = sueldoService.obtenerSueldosPorUsuario(1L);

        assertEquals(1, lista.size());
        assertEquals(1L, lista.get(0).getUsuarioId());
    }

    /**
     * Valida la eliminacion de un sueldo existente y la resincronizacion del sueldo del usuario
     * cuando ya no le queda ningun registro.
     */
    @Test
    void eliminarSueldo_Existente_EliminaYResincronizaUsuario() {
        when(sueldoRepository.findById(10L)).thenReturn(Optional.of(sueldo));
        when(sueldoRepository.findFirstByUsuario_IdOrderByAnioDescMesDesc(1L)).thenReturn(Optional.empty());

        sueldoService.eliminarSueldo(10L);

        verify(sueldoRepository).delete(sueldo);
        assertNull(usuario.getSueldo());
        verify(usuarioRepository).save(usuario);
    }

    /**
     * Valida que eliminar un sueldo inexistente lance RecursoNoEncontradoException.
     */
    @Test
    void eliminarSueldo_Inexistente_LanzaRecursoNoEncontradoException() {
        when(sueldoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sueldoService.eliminarSueldo(999L));
    }
}
