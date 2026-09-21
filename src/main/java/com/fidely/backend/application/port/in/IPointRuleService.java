package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrée permettant de gérer les règles d'attribution de points.
 */
public interface IPointRuleService {

    PointRule createPointRule(
            PointRule pointRule,
            UUID merchantId
    );

    Optional<PointRule> getPointRuleById(UUID pointRuleId);

    List<PointRule> getPointRulesByMerchant(UUID merchantId);

    Optional<PointRule> getValidPointRule(
            UUID merchantId,
            LocalDateTime date
    );

    void deletePointRule(UUID pointRuleId);
}