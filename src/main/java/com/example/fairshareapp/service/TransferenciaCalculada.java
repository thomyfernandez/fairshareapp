package com.example.fairshareapp.service;

import java.math.BigDecimal;

/**
 * Representa una transferencia simplificada resultante del algoritmo de consolidacion de saldos:
 * el usuario deudor debe transferir el monto indicado al usuario acreedor.
 *
 * @param deudorId Identificador del usuario que debe transferir el dinero.
 * @param acreedorId Identificador del usuario que debe recibir el dinero.
 * @param monto Monto a transferir.
 */
public record TransferenciaCalculada(Long deudorId, Long acreedorId, BigDecimal monto) {
}
