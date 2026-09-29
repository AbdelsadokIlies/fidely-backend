package com.fidely.backend.api.dtos.models.wheels;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Réponse contenant la configuration complète d'une roue.
 */
public record WheelResponse(
        UUID id,
        UUID merchantId,
        String name,
        boolean active,
        int minIntervalMinutes,
        boolean requiresValidatedPurchase,
        LocalDateTime createdAt,
        List<WheelPrizeResponse> prizes
) {
}