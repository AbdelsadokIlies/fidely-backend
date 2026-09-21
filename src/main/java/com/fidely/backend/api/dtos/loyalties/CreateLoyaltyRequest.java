package com.fidely.backend.api.dtos.loyalties;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLoyaltyRequest(

        @NotNull
        UUID customerId,

        @NotNull
        UUID merchantId
) {
}