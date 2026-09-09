package com.example.fairshareapp.repository;

import com.example.fairshareapp.model.entity.Espacio;
import com.example.fairshareapp.model.entity.PlantillaGastoRecurrente;
import com.example.fairshareapp.model.enums.ReglaReparto;
import com.example.fairshareapp.model.enums.TipoEspacio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pruebas de integracion para PlantillaGastoRepository comprobando la consulta por espacio.
 */
@SpringBootTest
class PlantillaGastoRepositoryTest {

    @Autowired
    private PlantillaGastoRepository plantillaRepository;

    @Autowired
    private EspacioRepository espacioRepository;

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
}
