package com.example.fairshareapp.exception;

import java.time.LocalDate;

/**
 * Excepcion lanzada al intentar ejecutar una plantilla de gasto recurrente cuyo ciclo de revision ya vencio.
 */
public class CicloVencidoException extends RuntimeException {

    /**
     * Constructor que arma el mensaje descriptivo del ciclo vencido.
     *
     * @param nombrePlantilla Nombre de la plantilla cuyo ciclo esta vencido.
     * @param fechaVencimiento Fecha en la que vencio el ciclo de revision.
     */
    public CicloVencidoException(String nombrePlantilla, LocalDate fechaVencimiento) {
        super("El ciclo de la plantilla '" + nombrePlantilla + "' vencio el " + fechaVencimiento
                + ". Debe actualizar la tarifa antes de ejecutar el gasto.");
    }
}
