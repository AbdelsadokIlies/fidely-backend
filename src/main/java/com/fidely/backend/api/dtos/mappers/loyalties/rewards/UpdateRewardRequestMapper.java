package com.fidely.backend.api.dtos.mappers.loyalties.rewards;

import com.fidely.backend.api.dtos.models.loyalties.rewards.UpdateRewardRequest;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Convertit une requête de modification de récompense en modèle de domaine.
 */
@Component
public class UpdateRewardRequestMapper {

    /**
     * Convertit une requête API en récompense.
     *
     * @param request requête de modification
     * @return récompense mise à jour
     */
    public Reward toDomain(
            UUID rewardId,
            UpdateRewardRequest request
    ) {
        if (rewardId == null) {
            throw new IllegalArgumentException(
                    "Reward id cannot be null"
            );
        }
        if (request == null) {
            throw new IllegalArgumentException(
                    "Update reward request cannot be null"
            );
        }

        return new Reward(
                rewardId,
                UUID.randomUUID(),
                request.name(),
                request.description(),
                request.costPoints(),
                request.quantityAvailable(),
                request.validFrom(),
                request.validTo(),
                request.active(),
                LocalDateTime.now()
        );
    }
}