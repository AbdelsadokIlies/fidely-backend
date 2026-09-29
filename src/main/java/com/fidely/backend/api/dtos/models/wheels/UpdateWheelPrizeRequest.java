package com.fidely.backend.api.dtos.models.wheels;

/**
 * Requête permettant de créer ou modifier un lot de roue.
 *
 * @param label libellé du lot
 * @param probabilityWeight poids utilisé pour le tirage aléatoire
 */
public record UpdateWheelPrizeRequest(
        String label,
        int probabilityWeight
) {
}