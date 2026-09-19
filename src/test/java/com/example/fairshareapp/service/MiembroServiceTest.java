package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.MembresiaDuplicadaException;
import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.ActualizarSueldoDTO;
import com.example.fairshareapp.model.dto.MiembroResponseDTO;
import com.example.fairshareapp.model.dto.UnirseEspacioDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.MiembroEspacio;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.RolMiembro;
import com.example.fairshareapp.model.enums.TipoEspacio;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.MiembroEspacioRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import com.example.fairshareapp.service.impl.MiembroServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para validar la logica de negocio de MiembroService,
 * cubriendo union a espacios, rechazo de membresias duplicadas, asignacion de roles,
 * actualizacion de sueldos y comprobacion de permisos administrativos.
 */
@ExtendWith(MockitoExtension.class)
class MiembroServiceTest {

    @Mock
    private MiembroEspacioRepository miembroEspacioRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MiembroServiceImpl miembroService;

    private Espacio espacioMock;
    private Usuario usuarioMock;
    private MiembroEspacio miembroMock;

    /**
     * Inicializa los datos base para cada prueba unitaria.
     */
    @BeforeEach
    void setUp() {
        espacioMock = Espacio.builder()
                .id(1L)
                .nombre("Depto Belgrano")
                .codigo("DEPTO-BEL")
                .tipo(TipoEspacio.HOGAR)
                .build();

        usuarioMock = Usuario.builder()
                .id(10L)
                .nombreUsuario("uriel")
                .email("uriel@example.com")
                .nombre("Uriel")
                .apellido("Perez")
                .build();

        miembroMock = MiembroEspacio.builder()
                .id(100L)
                .espacio(espacioMock)
                .usuario(usuarioMock)
                .rol(RolMiembro.ADMIN)
                .sueldoDeclarado(BigDecimal.valueOf(500000.00))
                .build();
    }

    /**
     * Valida que el primer miembro que se une al espacio sea registrado con rol ADMIN.
     */
    @Test
    void unirseAEspacio_PrimerMiembro_AsignaRolAdmin() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("DEPTO-BEL")
                .usuarioId(10L)
                .sueldoDeclarado(BigDecimal.valueOf(400000.00))
                .build();

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioMock));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(false);
        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(Collections.emptyList());
        when(miembroEspacioRepository.save(any(MiembroEspacio.class))).thenAnswer(invocation -> {
            MiembroEspacio m = invocation.getArgument(0);
            m.setId(101L);
            return m;
        });

        MiembroResponseDTO respuesta = miembroService.unirseAEspacio(1L, unirseDTO);

        assertNotNull(respuesta);
        assertEquals(101L, respuesta.getId());
        assertEquals(RolMiembro.ADMIN, respuesta.getRol());
        assertEquals(BigDecimal.valueOf(400000.00), respuesta.getSueldoDeclarado());
    }

    /**
     * Valida que los miembros subsecuentes al primero sean registrados con rol MIEMBRO.
     */
    @Test
    void unirseAEspacio_MiembroPosterior_AsignaRolMiembro() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("DEPTO-BEL")
                .usuarioId(10L)
                .build();

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioMock));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(false);
        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(List.of(miembroMock));
        when(miembroEspacioRepository.save(any(MiembroEspacio.class))).thenAnswer(invocation -> {
            MiembroEspacio m = invocation.getArgument(0);
            m.setId(102L);
            return m;
        });

        MiembroResponseDTO respuesta = miembroService.unirseAEspacio(1L, unirseDTO);

        assertNotNull(respuesta);
        assertEquals(RolMiembro.MIEMBRO, respuesta.getRol());
    }

    /**
     * Valida que se rechace la union cuando el codigo provisto no coincide con el del espacio.
     */
    @Test
    void unirseAEspacio_CodigoIncorrecto_LanzaReglaInvalidaException() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("COD-ERRONEO")
                .usuarioId(10L)
                .build();

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioMock));

        assertThrows(ReglaInvalidaException.class, () -> miembroService.unirseAEspacio(1L, unirseDTO));
    }

    /**
     * Valida que se lance MembresiaDuplicadaException (HTTP 409) si el usuario ya es miembro.
     */
    @Test
    void unirseAEspacio_MembresiaDuplicada_LanzaMembresiaDuplicadaException() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("DEPTO-BEL")
                .usuarioId(10L)
                .build();

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioMock));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(true);

        assertThrows(MembresiaDuplicadaException.class, () -> miembroService.unirseAEspacio(1L, unirseDTO));
    }

    /**
     * Valida que se rechace la union si el sueldo declarado es negativo.
     */
    @Test
    void unirseAEspacio_SueldoNegativo_LanzaReglaInvalidaException() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("DEPTO-BEL")
                .usuarioId(10L)
                .sueldoDeclarado(BigDecimal.valueOf(-500.00))
                .build();

        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioMock));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(false);

        assertThrows(ReglaInvalidaException.class, () -> miembroService.unirseAEspacio(1L, unirseDTO));
    }

    /**
     * Valida la union directa resolviendo el espacio a partir de su codigo de invitacion.
     */
    @Test
    void unirsePorCodigo_CodigoValido_SeUneExitosamente() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("depto-bel")
                .usuarioId(10L)
                .build();

        when(espacioRepository.findByCodigoIgnoreCase("depto-bel")).thenReturn(Optional.of(espacioMock));
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioMock));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(false);
        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(List.of(miembroMock));
        when(miembroEspacioRepository.save(any(MiembroEspacio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MiembroResponseDTO respuesta = miembroService.unirsePorCodigo(unirseDTO);

        assertNotNull(respuesta);
        assertEquals(RolMiembro.MIEMBRO, respuesta.getRol());
    }

    /**
     * Valida que unirsePorCodigo con codigo nulo o vacio lance ReglaInvalidaException.
     */
    @Test
    void unirsePorCodigo_CodigoVacio_LanzaReglaInvalidaException() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("   ")
                .usuarioId(10L)
                .build();

        assertThrows(ReglaInvalidaException.class, () -> miembroService.unirsePorCodigo(unirseDTO));
    }

    /**
     * Valida que unirsePorCodigo con un codigo que no existe lance RecursoNoEncontradoException.
     */
    @Test
    void unirsePorCodigo_CodigoInexistente_LanzaRecursoNoEncontradoException() {
        UnirseEspacioDTO unirseDTO = UnirseEspacioDTO.builder()
                .codigo("NO-EXISTE")
                .usuarioId(10L)
                .build();

        when(espacioRepository.findByCodigoIgnoreCase("NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> miembroService.unirsePorCodigo(unirseDTO));
    }

    /**
     * Valida la obtencion del listado de miembros de un espacio existente.
     */
    @Test
    void listarMiembros_EspacioExistente_RetornaLista() {
        when(espacioRepository.existsById(1L)).thenReturn(true);
        when(miembroEspacioRepository.findByEspacioId(1L)).thenReturn(List.of(miembroMock));

        List<MiembroResponseDTO> miembros = miembroService.listarMiembros(1L);

        assertNotNull(miembros);
        assertEquals(1, miembros.size());
        assertEquals("Uriel Perez", miembros.get(0).getNombreUsuario());
    }

    /**
     * Valida que al listar miembros de un espacio inexistente se arroje RecursoNoEncontradoException.
     */
    @Test
    void listarMiembros_EspacioInexistente_LanzaRecursoNoEncontradoException() {
        when(espacioRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> miembroService.listarMiembros(99L));
    }

    /**
     * Valida la actualizacion exitosa del sueldo declarado de un miembro.
     */
    @Test
    void actualizarSueldo_DatosValidos_ActualizaSueldo() {
        ActualizarSueldoDTO dto = ActualizarSueldoDTO.builder()
                .sueldoDeclarado(BigDecimal.valueOf(650000.00))
                .build();

        when(miembroEspacioRepository.findByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(Optional.of(miembroMock));
        when(miembroEspacioRepository.save(any(MiembroEspacio.class))).thenAnswer(i -> i.getArgument(0));

        MiembroResponseDTO respuesta = miembroService.actualizarSueldo(1L, 10L, dto);

        assertNotNull(respuesta);
        assertEquals(BigDecimal.valueOf(650000.00), respuesta.getSueldoDeclarado());
    }

    /**
     * Valida que no se permita registrar un sueldo nulo o negativo.
     */
    @Test
    void actualizarSueldo_SueldoNegativo_LanzaReglaInvalidaException() {
        ActualizarSueldoDTO dto = ActualizarSueldoDTO.builder()
                .sueldoDeclarado(BigDecimal.valueOf(-1.00))
                .build();

        assertThrows(ReglaInvalidaException.class, () -> miembroService.actualizarSueldo(1L, 10L, dto));
    }

    /**
     * Valida la asignacion de un nuevo rol a un miembro cuando el solicitante es ADMIN.
     */
    @Test
    void asignarRol_ConSolicitanteAdmin_AsignaRolCorrectamente() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 10L, RolMiembro.ADMIN)).thenReturn(true);

        MiembroEspacio otroMiembro = MiembroEspacio.builder()
                .id(105L)
                .espacio(espacioMock)
                .usuario(Usuario.builder().id(20L).nombreUsuario("carlos").build())
                .rol(RolMiembro.MIEMBRO)
                .build();

        when(miembroEspacioRepository.findByEspacioIdAndUsuarioId(1L, 20L)).thenReturn(Optional.of(otroMiembro));
        when(miembroEspacioRepository.save(any(MiembroEspacio.class))).thenAnswer(i -> i.getArgument(0));

        MiembroResponseDTO respuesta = miembroService.asignarRol(1L, 20L, RolMiembro.ADMIN, 10L);

        assertNotNull(respuesta);
        assertEquals(RolMiembro.ADMIN, respuesta.getRol());
    }

    /**
     * Valida que se deniegue la asignacion de roles si el solicitante no es administrador.
     */
    @Test
    void asignarRol_ConSolicitanteNoAdmin_LanzaReglaInvalidaException() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 20L)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 20L, RolMiembro.ADMIN)).thenReturn(false);

        assertThrows(ReglaInvalidaException.class, () -> miembroService.asignarRol(1L, 30L, RolMiembro.ADMIN, 20L));
    }

    /**
     * Valida que la verificacion de pertenencia no lance excepcion cuando el usuario es miembro.
     */
    @Test
    void validarPertenencia_MiembroExiste_NoLanzaExcepcion() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(true);

        assertDoesNotThrow(() -> miembroService.validarPertenencia(1L, 10L));
    }

    /**
     * Valida que la verificacion de pertenencia lance RecursoNoEncontradoException si no es miembro.
     */
    @Test
    void validarPertenencia_NoEsMiembro_LanzaRecursoNoEncontradoException() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> miembroService.validarPertenencia(1L, 99L));
    }

    /**
     * Valida las comprobaciones booleanas de esMiembro y esAdmin.
     */
    @Test
    void comprobacionesBooleanas_RetornanValoresCorrectos() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 10L, RolMiembro.ADMIN)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 99L)).thenReturn(false);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 99L, RolMiembro.ADMIN)).thenReturn(false);

        assertTrue(miembroService.esMiembro(1L, 10L));
        assertTrue(miembroService.esAdmin(1L, 10L));
        assertFalse(miembroService.esMiembro(1L, 99L));
        assertFalse(miembroService.esAdmin(1L, 99L));
    }
}
