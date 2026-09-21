package com.fidely.backend.api.dtos.loyalties;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requête de création d'un programme de fidélité.
 *
 * @param customerId identifiant du client
 * @param merchantId identifiant du marchand
 */
public record CreateLoyaltyRequest(

        @NotNull
        UUID customerId,

        @NotNull
        UUID merchantId
) {
}