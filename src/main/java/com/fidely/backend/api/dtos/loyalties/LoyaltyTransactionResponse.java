package com.fidely.backend.api.dtos.loyalties;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoyaltyTransactionResponse(
        UUID id,
        UUID loyaltyId,
        UUID ticketId,
        int points,
        String description,
        LocalDateTime createdAt
) {}