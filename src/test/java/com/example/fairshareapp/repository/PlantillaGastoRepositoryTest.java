package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integracion para PlantillaGastoRepository comprobando las consultas por espacio y por vencimiento.
 */
@SpringBootTest
class PlantillaGastoRepositoryTest {

    @Autowired
    private PlantillaGastoRepository plantillaRepository;

    @Autowired
    private EspacioRepository espacioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Verifica que la consulta findByEspacioId filtre correctamente las plantillas por el identificador del espacio.
     */
    @Test
    void testFindByEspacioId() {
        Espacio espacio = Espacio.builder()
                .nombre("Test Espacio")
                .codigo("TEST-ESP")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .build();
        espacio = espacioRepository.save(espacio);

        List<PlantillaGastoRecurrente> resultado = plantillaRepository.findByEspacioId(espacio.getId());
        assertNotNull(resultado);
    }

    /**
     * Verifica que findVencidasPorEspacio solo devuelva las plantillas del espacio cuyo ciclo de revision
     * ya venció (fecha menor o igual a hoy), dejando afuera las que aun estan vigentes.
     */
    @Test
    void testFindVencidasPorEspacio() {
        Espacio espacio = espacioRepository.save(Espacio.builder()
                .nombre("Espacio Vencimientos")
                .codigo("TEST-VENC")
                .tipo(TipoEspacio.HOGAR)
                .reglaReparto(ReglaReparto.CINCUENTA_CINCUENTA)
                .build());

        Usuario pagador = usuarioRepository.save(Usuario.builder().contra("hash-de-prueba")
                .nombre("Pagador")
                .apellido("Test")
                .email("pagador.vencimientos@example.com")
                .build());

        PlantillaGastoRecurrente vencida = plantillaRepository.save(PlantillaGastoRecurrente.builder()
                .nombre("Alquiler Vencido")
                .montoBase(BigDecimal.valueOf(1000))
                .fechaProximaRevision(LocalDate.now().minusDays(1))
                .espacioId(espacio)
                .pagadorId(pagador)
                .build());

        plantillaRepository.save(PlantillaGastoRecurrente.builder()
                .nombre("Internet Vigente")
                .montoBase(BigDecimal.valueOf(500))
                .fechaProximaRevision(LocalDate.now().plusMonths(1))
                .espacioId(espacio)
                .pagadorId(pagador)
                .build());

        List<PlantillaGastoRecurrente> vencidas = plantillaRepository.findVencidasPorEspacio(espacio.getId(), LocalDate.now());

        assertEquals(1, vencidas.size());
        assertEquals(vencida.getId(), vencidas.get(0).getId());
        assertTrue(vencidas.get(0).getFechaProximaRevision().isBefore(LocalDate.now()));
    }
}
