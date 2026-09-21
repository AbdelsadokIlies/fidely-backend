package com.fidely.backend.api.dtos.loyalties.rewards;

import jakarta.validation.constraints.Positive;

public record PointsRequest(

        @Positive
        int points
) {
}