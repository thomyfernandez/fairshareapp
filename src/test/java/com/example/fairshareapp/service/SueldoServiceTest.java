package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.FrecuenciaSueldo;
import com.example.fairshareapp.model.enums.TipoSueldo;
import com.example.fairshareapp.model.mapper.SueldoMapper;
import com.example.fairshareapp.model.request.SueldoRequest;
import com.example.fairshareapp.model.response.SueldoResponse;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
                .usuario(usuario)
                .build();
    }

    /**
     * Valida la creacion exitosa de un sueldo y la actualizacion del sueldo en el usuario.
     */
    @Test
    void crearSueldo_UsuarioExistente_CreaSueldoYSincronizaUsuario() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(850000.00));
        request.setTipo(TipoSueldo.FIJO);
        request.setFrecuencia(FrecuenciaSueldo.MENSUAL);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(sueldoRepository.save(any(Sueldo.class))).thenAnswer(i -> {
            Sueldo s = i.getArgument(0);
            s.setId(11L);
            return s;
        });

        SueldoResponse response = sueldoService.crearSueldo(1L, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(850000.00), response.getMonto());
        assertEquals(BigDecimal.valueOf(850000.00), usuario.getSueldo());
        verify(usuarioRepository).save(usuario);
        verify(sueldoRepository).save(any(Sueldo.class));
    }

    /**
     * Valida que se lance RecursoNoEncontradoException cuando el usuario no existe.
     */
    @Test
    void crearSueldo_UsuarioInexistente_LanzaRecursoNoEncontradoException() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(500000.00));

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sueldoService.crearSueldo(99L, request));
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
     * Valida la actualizacion de los valores de un sueldo.
     */
    @Test
    void actualizarSueldo_Existente_ActualizaValores() {
        SueldoRequest request = new SueldoRequest();
        request.setMonto(BigDecimal.valueOf(900000.00));
        request.setTipo(TipoSueldo.VARIABLE);
        request.setFrecuencia(FrecuenciaSueldo.QUINCENAL);

        when(sueldoRepository.findById(10L)).thenReturn(Optional.of(sueldo));

        sueldoService.actualizarSueldo(10L, request);

        assertEquals(BigDecimal.valueOf(900000.00), sueldo.getMonto());
        assertEquals(TipoSueldo.VARIABLE, sueldo.getTipo());
        assertEquals(FrecuenciaSueldo.QUINCENAL, sueldo.getFrecuencia());
        verify(sueldoRepository).save(sueldo);
    }

    /**
     * Valida el listado completo de sueldos disponibles.
     */
    @Test
    void getAllSueldos_RetornaLista() {
        when(sueldoRepository.findAll()).thenReturn(List.of(sueldo));

        List<SueldoResponse> lista = sueldoService.getAllSueldos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(10L, lista.get(0).getId());
    }

    /**
     * Valida la eliminacion de un sueldo existente.
     */
    @Test
    void eliminarSueldo_Existente_EliminaCorrectamente() {
        when(sueldoRepository.existsById(10L)).thenReturn(true);

        sueldoService.eliminarSueldo(10L);

        verify(sueldoRepository).deleteById(10L);
    }
}
