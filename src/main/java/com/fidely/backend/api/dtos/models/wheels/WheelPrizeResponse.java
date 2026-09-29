package com.fidely.backend.api.dtos.models.wheels;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse contenant les informations d'un lot de roue.
 */
public record WheelPrizeResponse(
        UUID id,
        UUID wheelId,
        String label,
        int probabilityWeight,
        LocalDateTime createdAt
) {
}