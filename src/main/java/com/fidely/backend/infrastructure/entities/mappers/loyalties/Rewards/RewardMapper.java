package com.fidely.backend.infrastructure.mappers.loyalties.Rewards;

import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import com.fidely.backend.infrastructure.entities.models.loyalties.Rewards.RewardEntity;
import org.springframework.stereotype.Component;

/**
 * Convertit une récompense entre le modèle de domaine et l'entité JPA.
 */
@Component
public class RewardMapper {

    /**
     * Convertit une entité JPA en modèle de domaine.
     *
     * @param entity entité à convertir
     * @return récompense du domaine
     */
    public Reward toDomain(RewardEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Reward entity cannot be null"
            );
        }

        return new Reward(
                entity.getId(),
                entity.getMerchantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCostPoints(),
                entity.getQuantityAvailable(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit un modèle de domaine en entité JPA.
     *
     * @param reward récompense à convertir
     * @return entité JPA
     */
    public RewardEntity toEntity(Reward reward) {
        if (reward == null) {
            throw new IllegalArgumentException(
                    "Reward cannot be null"
            );
        }

        return new RewardEntity(
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