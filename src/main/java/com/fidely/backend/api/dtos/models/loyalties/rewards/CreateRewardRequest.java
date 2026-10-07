package com.fidely.backend.api.dtos.models.rewards;

/**
 * Requête permettant de créer une récompense de fidélité.
 */
public record CreateRewardRequest(
        String name,
        String description,
        int costPoints,
        Integer quantityAvailable,
        java.time.LocalDateTime validFrom,
        java.time.LocalDateTime validTo,
        boolean active
) {
}