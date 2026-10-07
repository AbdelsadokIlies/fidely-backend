package com.fidely.backend.api.dtos.mappers.loyalties.rewards;

import com.fidely.backend.api.dtos.models.loyalties.rewards.RewardResponse;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import org.springframework.stereotype.Component;

/**
 * Convertit les modèles de domaine Reward en réponses API.
 */
@Component
public class RewardResponseMapper {

    /**
     * Convertit une récompense en réponse API.
     *
     * @param reward récompense à convertir
     * @return réponse API
     */
    public RewardResponse toResponse(Reward reward) {
        if (reward == null) {
            throw new IllegalArgumentException(
                    "Reward cannot be null"
            );
        }

        return new RewardResponse(
                reward.getId(),
                reward.getMerchantId(),
                reward.getName(),
                reward.getDescription(),
                reward.getCostPoints(),
                reward.getQuantityAvailable(),
                reward.getValidFrom(),
                reward.getValidTo(),
                reward.isActive(),
                reward.getCreatedAt()
        );
    }
}