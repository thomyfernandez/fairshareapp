package com.example.fairshareapp.exception;

/**
 * Excepcion lanzada al intentar registrar un gasto fechado dentro de un periodo ya cerrado.
 */
public class PeriodoCerradoException extends RuntimeException {

    /**
     * Constructor que arma el mensaje descriptivo del periodo cerrado involucrado.
     *
     * @param espacioId Identificador del espacio al que pertenece el periodo.
     * @param anio Anio del periodo cerrado.
     * @param mes Mes del periodo cerrado.
     */
    public PeriodoCerradoException(Long espacioId, Integer anio, Integer mes) {
        super("El periodo " + mes + "/" + anio + " del espacio con id " + espacioId +
                " se encuentra cerrado y no admite nuevos gastos");
    }
}
