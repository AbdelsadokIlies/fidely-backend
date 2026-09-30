package com.fidely.backend.api.dtos.models.loyalties.rewards;

import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse contenant les informations d'une règle d'attribution de points.
 */
public record PointRuleResponse(
        UUID id,
        BigDecimal pointsPerCurrencyUnit,
        RoundingMethod roundingMethod,
        boolean active,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        LocalDateTime createdAt
) {
}