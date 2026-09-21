package com.fidely.backend.api.dtos.loyalties;

import java.util.UUID;

/**
 * Réponse contenant les informations d'un programme de fidélité.
 *
 * @param id identifiant du programme de fidélité
 * @param customerId identifiant du client
 * @param merchantId identifiant du marchand
 * @param pointsBalance solde actuel de points
 */
public record LoyaltyResponse(
        UUID id,
        UUID customerId,
        UUID merchantId,
        int pointsBalance
) {
}