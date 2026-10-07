package com.fidely.backend.api.dtos.models.loyalties.rewards;

import com.fidely.backend.domain.models.loyalties.Rewards.Reward;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse API représentant une récompense de fidélité.
 */
public record RewardResponse(
        UUID id,
        UUID merchantId,
        String name,
        String description,
        int costPoints,
        Integer quantityAvailable,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        boolean active,
        LocalDateTime createdAt
) {
}