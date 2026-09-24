package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.loyalties.Rewards.PointRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux règles d'attribution de points.
 */
public interface SpringDataPointRuleRepository
        extends JpaRepository<PointRuleEntity, UUID> {

    List<PointRuleEntity> findByMerchantId(UUID merchantId);
}