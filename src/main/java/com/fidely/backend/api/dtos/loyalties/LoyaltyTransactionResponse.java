package com.fidely.backend.api.dtos.loyalties;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse contenant les informations d'une transaction de fidélité.
 *
 * @param id identifiant de la transaction
 * @param loyaltyId identifiant du programme de fidélité
 * @param ticketId identifiant du ticket associé
 * @param points nombre de points de la transaction
 * @param description description de la transaction
 * @param createdAt date de création de la transaction
 */
public record LoyaltyTransactionResponse(
        UUID id,
        UUID loyaltyId,
        UUID ticketId,
        int points,
        String description,
        LocalDateTime createdAt
) {
}