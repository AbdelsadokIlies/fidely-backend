package com.fidely.backend.api.dtos.loyalties;

import java.util.UUID;

public record LoyaltyResponse(
        UUID id,
        UUID customerId,
        UUID merchantId,
        int pointsBalance
) {
}