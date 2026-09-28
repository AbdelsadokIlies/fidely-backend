package com.fidely.backend.api.dtos.models.merchants;

public record UpdateMerchantRequest(
        String name,
        String slug,
        String description,
        String googleReviewUrl
) {
}