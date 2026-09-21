package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant l'accès aux règles d'attribution de points.
 */
public interface IPointRuleRepository {

    Optional<PointRule> findById(UUID id);

    List<PointRule> findByMerchantId(UUID merchantId);

    PointRule save(PointRule pointRule, UUID merchantId);

    void deleteById(UUID id);
}