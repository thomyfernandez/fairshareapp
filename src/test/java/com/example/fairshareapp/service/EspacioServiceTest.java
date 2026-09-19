package com.example.fairshareapp.service;

import com.example.fairshareapp.exception.RecursoNoEncontradoException;
import com.example.fairshareapp.exception.ReglaInvalidaException;
import com.example.fairshareapp.model.dto.EspacioCreateDTO;
import com.example.fairshareapp.model.dto.EspacioResponseDTO;
import com.example.fairshareapp.model.dto.EspacioUpdateDTO;
import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import com.example.fairshareapp.model.enums.RolMiembro;
import com.example.fairshareapp.repository.EspacioRepository;
import com.example.fairshareapp.repository.MiembroEspacioRepository;
import com.example.fairshareapp.service.impl.EspacioServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para validar la logica de negocio de EspacioService,
 * cubriendo creacion, edicion de reglas (50/50 vs Proporcional), fijacion de presupuesto base
 * y verificacion de permisos de administracion.
 */
@ExtendWith(MockitoExtension.class)
class EspacioServiceTest {

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private MiembroEspacioRepository miembroEspacioRepository;

    @InjectMocks
    private EspacioServiceImpl espacioService;

    private Espacio espacioExistente;

    /**
     * Configuracion previa para cada escenario de prueba.
     */
    @BeforeEach
    void setUp() {
        espacioExistente = Espacio.builder()
                .id(1L)
                .nombre("Depto Palermo")
                .descripcion("Gastos compartidos convivencia")
                .codigo("DEPTO-PAL")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .presupuestoBase(BigDecimal.valueOf(150000.00))
                .build();
    }

    /**
     * Valida la creacion exitosa de un espacio con codigo y presupuesto provistos.
     */
    @Test
    void crearEspacio_DatosValidos_RetornaResponseDTO() {
        EspacioCreateDTO createDTO = EspacioCreateDTO.builder()
                .nombre("Viaje Bariloche")
                .descripcion("Vacaciones de invierno")
                .codigo("BARI-2026")
                .tipo(TipoEspacio.VIAJE)
                .reglaReparto(ReglaReparto.PROPORCIONAL)
                .presupuestoBase(BigDecimal.valueOf(500000.00))
                .build();

        when(espacioRepository.existsByNombre("Viaje Bariloche")).thenReturn(false);
        when(espacioRepository.existsByCodigo("BARI-2026")).thenReturn(false);
        when(espacioRepository.save(any(Espacio.class))).thenAnswer(invocation -> {
            Espacio guardado = invocation.getArgument(0);
            guardado.setId(2L);
            return guardado;
        });

        EspacioResponseDTO resultado = espacioService.crearEspacio(createDTO);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals("Viaje Bariloche", resultado.getNombre());
        assertEquals("BARI-2026", resultado.getCodigo());
        assertEquals(TipoEspacio.VIAJE, resultado.getTipo());
        assertEquals(ReglaReparto.PROPORCIONAL, resultado.getReglaReparto());
        assertEquals(BigDecimal.valueOf(500000.00), resultado.getPresupuestoBase());
    }

    /**
     * Valida que se genere un codigo automatico si no se especifica al crear un espacio.
     */
    @Test
    void crearEspacio_SinCodigo_GeneraCodigoAutomatico() {
        EspacioCreateDTO createDTO = EspacioCreateDTO.builder()
                .nombre("Casa Central")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .build();

        when(espacioRepository.existsByNombre("Casa Central")).thenReturn(false);
        when(espacioRepository.existsByCodigo(any())).thenReturn(false);
        when(espacioRepository.save(any(Espacio.class))).thenAnswer(invocation -> {
            Espacio guardado = invocation.getArgument(0);
            guardado.setId(3L);
            return guardado;
        });

        EspacioResponseDTO resultado = espacioService.crearEspacio(createDTO);

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigo());
        assertEquals(TipoEspacio.HOGAR, resultado.getTipo());
    }

    /**
     * Valida que se arroje excepcion si ya existe un espacio con el mismo nombre.
     */
    @Test
    void crearEspacio_NombreDuplicado_LanzaReglaInvalidaException() {
        EspacioCreateDTO createDTO = EspacioCreateDTO.builder()
                .nombre("Depto Palermo")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .build();

        when(espacioRepository.existsByNombre("Depto Palermo")).thenReturn(true);

        assertThrows(ReglaInvalidaException.class, () -> espacioService.crearEspacio(createDTO));
    }

    /**
     * Valida que se arroje excepcion si ya existe un espacio con el mismo codigo.
     */
    @Test
    void crearEspacio_CodigoDuplicado_LanzaReglaInvalidaException() {
        EspacioCreateDTO createDTO = EspacioCreateDTO.builder()
                .nombre("Nuevo Depto")
                .codigo("DEPTO-PAL")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .build();

        when(espacioRepository.existsByNombre("Nuevo Depto")).thenReturn(false);
        when(espacioRepository.existsByCodigo("DEPTO-PAL")).thenReturn(true);

        assertThrows(ReglaInvalidaException.class, () -> espacioService.crearEspacio(createDTO));
    }

    /**
     * Valida la obtencion de un espacio por su id.
     */
    @Test
    void obtenerEspacioPorId_Existente_RetornaDTO() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioExistente));

        EspacioResponseDTO resultado = espacioService.obtenerEspacioPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Depto Palermo", resultado.getNombre());
    }

    /**
     * Valida que se lance RecursoNoEncontradoException cuando el id no existe.
     */
    @Test
    void obtenerEspacioPorId_Inexistente_LanzaRecursoNoEncontradoException() {
        when(espacioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> espacioService.obtenerEspacioPorId(99L));
    }

    /**
     * Valida la edicion de la regla de distribucion (de 50/50 a Proporcional).
     */
    @Test
    void editarReglaDistribucion_CambiaReglaCorrectamente() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioExistente));
        when(espacioRepository.save(any(Espacio.class))).thenAnswer(i -> i.getArgument(0));

        EspacioResponseDTO resultado = espacioService.editarReglaDistribucion(1L, ReglaReparto.PROPORCIONAL);

        assertNotNull(resultado);
        assertEquals(ReglaReparto.PROPORCIONAL, resultado.getReglaReparto());
    }

    /**
     * Valida la fijacion de un presupuesto base en el espacio.
     */
    @Test
    void fijarPresupuestoBase_MontoValido_ActualizaPresupuesto() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioExistente));
        when(espacioRepository.save(any(Espacio.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal nuevoPresupuesto = BigDecimal.valueOf(250000.00);
        EspacioResponseDTO resultado = espacioService.fijarPresupuestoBase(1L, nuevoPresupuesto);

        assertNotNull(resultado);
        assertEquals(nuevoPresupuesto, resultado.getPresupuestoBase());
    }

    /**
     * Valida que se arroje excepcion si se intenta fijar un presupuesto base negativo.
     */
    @Test
    void fijarPresupuestoBase_MontoNegativo_LanzaExcepcion() {
        BigDecimal presupuestoNegativo = BigDecimal.valueOf(-100.0);

        assertThrows(ReglaInvalidaException.class, () -> espacioService.fijarPresupuestoBase(1L, presupuestoNegativo));
    }

    /**
     * Valida la actualizacion completa de un espacio existente sin solicitante.
     */
    @Test
    void actualizarEspacio_DatosValidos_RetornaActualizado() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioExistente));
        when(espacioRepository.existsByNombreIgnoreCaseAndIdNot("Depto Renovado", 1L)).thenReturn(false);
        when(espacioRepository.save(any(Espacio.class))).thenAnswer(i -> i.getArgument(0));

        EspacioUpdateDTO updateDTO = EspacioUpdateDTO.builder()
                .nombre("Depto Renovado")
                .reglaReparto(ReglaReparto.PROPORCIONAL)
                .presupuestoBase(BigDecimal.valueOf(300000.00))
                .build();

        EspacioResponseDTO resultado = espacioService.actualizarEspacio(1L, updateDTO);

        assertNotNull(resultado);
        assertEquals("Depto Renovado", resultado.getNombre());
        assertEquals(ReglaReparto.PROPORCIONAL, resultado.getReglaReparto());
        assertEquals(BigDecimal.valueOf(300000.00), resultado.getPresupuestoBase());
    }

    /**
     * Valida la actualizacion de un espacio cuando el solicitante tiene rol de ADMIN.
     */
    @Test
    void actualizarEspacio_ConSolicitanteAdmin_RetornaActualizado() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 10L, RolMiembro.ADMIN)).thenReturn(true);
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioExistente));
        when(espacioRepository.save(any(Espacio.class))).thenAnswer(i -> i.getArgument(0));

        EspacioUpdateDTO updateDTO = EspacioUpdateDTO.builder()
                .descripcion("Nueva descripcion")
                .build();

        EspacioResponseDTO resultado = espacioService.actualizarEspacio(1L, updateDTO, 10L);

        assertNotNull(resultado);
        assertEquals("Nueva descripcion", resultado.getDescripcion());
    }

    /**
     * Valida que se rechace la actualizacion si el solicitante no tiene rol de ADMIN.
     */
    @Test
    void actualizarEspacio_ConSolicitanteNoAdmin_LanzaReglaInvalidaException() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 10L)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 10L, RolMiembro.ADMIN)).thenReturn(false);

        EspacioUpdateDTO updateDTO = EspacioUpdateDTO.builder()
                .descripcion("Intento sin permisos")
                .build();

        assertThrows(ReglaInvalidaException.class, () -> espacioService.actualizarEspacio(1L, updateDTO, 10L));
    }

    /**
     * Valida que se rechace la actualizacion si el solicitante no pertenece al espacio.
     */
    @Test
    void actualizarEspacio_ConSolicitanteNoMiembro_LanzaRecursoNoEncontradoException() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 99L)).thenReturn(false);

        EspacioUpdateDTO updateDTO = EspacioUpdateDTO.builder()
                .descripcion("Intento no miembro")
                .build();

        assertThrows(RecursoNoEncontradoException.class, () -> espacioService.actualizarEspacio(1L, updateDTO, 99L));
    }

    /**
     * Valida la obtencion de un espacio por su codigo unico.
     */
    @Test
    void obtenerEspacioPorCodigo_Existente_RetornaDTO() {
        when(espacioRepository.findByCodigo("DEPTO-PAL")).thenReturn(Optional.of(espacioExistente));

        EspacioResponseDTO resultado = espacioService.obtenerEspacioPorCodigo("DEPTO-PAL");

        assertNotNull(resultado);
        assertEquals("DEPTO-PAL", resultado.getCodigo());
        assertEquals("Depto Palermo", resultado.getNombre());
    }

    /**
     * Valida que obtener un espacio por codigo inexistente lance RecursoNoEncontradoException.
     */
    @Test
    void obtenerEspacioPorCodigo_Inexistente_LanzaRecursoNoEncontradoException() {
        when(espacioRepository.findByCodigo("COD-FALSO")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> espacioService.obtenerEspacioPorCodigo("COD-FALSO"));
    }

    /**
     * Valida el listado de todos los espacios disponibles.
     */
    @Test
    void listarEspacios_RetornaLista() {
        when(espacioRepository.findAll()).thenReturn(List.of(espacioExistente));

        List<EspacioResponseDTO> resultado = espacioService.listarEspacios();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    /**
     * Valida la eliminacion exitosa de un espacio sin solicitante.
     */
    @Test
    void eliminarEspacio_Existente_EliminaCorrectamente() {
        when(espacioRepository.existsById(1L)).thenReturn(true);

        espacioService.eliminarEspacio(1L);

        verify(espacioRepository).deleteById(1L);
    }

    /**
     * Valida la eliminacion de un espacio cuando el solicitante tiene rol de ADMIN.
     */
    @Test
    void eliminarEspacio_ConSolicitanteAdmin_EliminaCorrectamente() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 5L)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 5L, RolMiembro.ADMIN)).thenReturn(true);
        when(espacioRepository.existsById(1L)).thenReturn(true);

        espacioService.eliminarEspacio(1L, 5L);

        verify(espacioRepository).deleteById(1L);
    }

    /**
     * Valida que se rechace la eliminacion de un espacio si el solicitante no es ADMIN.
     */
    @Test
    void eliminarEspacio_ConSolicitanteNoAdmin_LanzaReglaInvalidaException() {
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioId(1L, 5L)).thenReturn(true);
        when(miembroEspacioRepository.existsByEspacioIdAndUsuarioIdAndRol(1L, 5L, RolMiembro.ADMIN)).thenReturn(false);

        assertThrows(ReglaInvalidaException.class, () -> espacioService.eliminarEspacio(1L, 5L));
    }
}
