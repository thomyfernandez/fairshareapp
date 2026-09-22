package com.example.fairshareapp.model.response;

/**
 * Resultado de una operacion de creacion o actualizacion (upsert) de sueldo,
 * indicando el DTO resultante y si el registro fue creado o actualizado.
 *
 * @param sueldo  Datos del sueldo resultante.
 * @param creado true si se creo un nuevo registro, false si se actualizo uno existente.
 */
public record SueldoUpsertResult(SueldoResponse sueldo, boolean creado) {
}
