package com.fidely.backend.api.dtos.models.transactions;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse retournée après la création d'une transaction
 * de fidélité.
 *
 * <p>Cette réponse expose l'état actualisé du programme de fidélité
 * après l'attribution des points liés au ticket.</p>
 *
 * @param loyaltyId identifiant du programme de fidélité
 * @param customerId identifiant du client
 * @param merchantId identifiant du marchand
 * @param pointsBalance nouveau solde de points
 * @param updatedAt date de dernière modification du solde
 */
public record CreateTransactionResponse(
        UUID loyaltyId,
        UUID customerId,
        UUID merchantId,
        int pointsBalance,
        LocalDateTime updatedAt
) {}