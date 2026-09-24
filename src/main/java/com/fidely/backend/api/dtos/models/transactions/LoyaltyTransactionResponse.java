package com.fidely.backend.api.dtos.models.transactions;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse API représentant une transaction de points de fidélité.
 *
 * <p>Cette réponse expose les informations nécessaires à la consultation
 * d'une transaction de fidélité par un client ou un marchand.</p>
 *
 * @param id identifiant de la transaction
 * @param loyaltyId identifiant du programme de fidélité associé
 * @param ticketId identifiant du ticket à l'origine de la transaction,
 *                ou {@code null} pour une transaction manuelle
 * @param points nombre de points associés à la transaction
 * @param description description de la transaction
 * @param createdAt date et heure de création de la transaction
 */
public record LoyaltyTransactionResponse(
        UUID id,
        UUID loyaltyId,
        UUID ticketId,
        int points,
        String description,
        LocalDateTime createdAt
) { }