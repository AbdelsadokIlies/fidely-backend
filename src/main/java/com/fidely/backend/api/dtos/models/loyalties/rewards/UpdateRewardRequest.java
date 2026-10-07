package com.fidely.backend.api.dtos.models.loyalties.rewards;

/**
 * Requête permettant de modifier une récompense de fidélité.
 */
public record UpdateRewardRequest(
        String name,
        String description,
        int costPoints,
        Integer quantityAvailable,
        java.time.LocalDateTime validFrom,
        java.time.LocalDateTime validTo,
        boolean active
) {
}