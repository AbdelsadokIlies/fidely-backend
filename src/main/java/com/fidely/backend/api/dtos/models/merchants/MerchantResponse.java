package com.fidely.backend.api.dtos.models.merchants;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse contenant les informations du marchand connecté.
 */
public record MerchantResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String description,
        String googleReviewUrl,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}