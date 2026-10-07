package com.fidely.backend.infrastructure.repositories.loyalties.Rewards;

import com.fidely.backend.infrastructure.entities.models.loyalties.Rewards.RewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository Spring Data permettant d'accéder aux récompenses en base de données.
 */
public interface SpringDataRewardRepository
        extends JpaRepository<RewardEntity, UUID> {

    /**
     * Recherche toutes les récompenses d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des récompenses du marchand
     */
    List<RewardEntity> findByMerchantId(UUID merchantId);
}