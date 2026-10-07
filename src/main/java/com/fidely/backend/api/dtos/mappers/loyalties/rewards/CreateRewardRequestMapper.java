package com.fidely.backend.api.dtos.mappers.rewards;

import com.fidely.backend.api.dtos.models.rewards.CreateRewardRequest;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Convertit une requête de création de récompense en modèle de domaine.
 */
@Component
public class CreateRewardRequestMapper {

    /**
     * Convertit une requête API en récompense.
     *
     * @param request requête de création
     * @return récompense du domaine
     */
    public Reward toDomain(
            CreateRewardRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Create reward request cannot be null"
            );
        }

        return new Reward(
                UUID.randomUUID(),
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