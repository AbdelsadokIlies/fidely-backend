package com.fidely.backend.api.dtos.models.wheels;

/**
 * Requête permettant de créer ou modifier la configuration d'une roue.
 *
 * @param name nom de la roue
 * @param active indique si la roue est active
 * @param minIntervalMinutes intervalle minimal entre deux participations
 * @param requiresValidatedPurchase indique si un achat validé est requis
 */
public record UpdateWheelRequest(
        String name,
        boolean active,
        int minIntervalMinutes,
        boolean requiresValidatedPurchase
) {
}