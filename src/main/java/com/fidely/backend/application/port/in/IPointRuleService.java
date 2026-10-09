package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrée permettant de gérer les règles d'attribution de points.
 */
public interface IPointRuleService {

    PointRule createPointRule(
            UUID userId,
            BigDecimal pointsPerCurrencyUnit,
            RoundingMethod roundingMethod,
            boolean active,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime dateTime
    );

    List<PointRule> getPointRulesByMerchant(UUID merchantId);

    Optional<PointRule> getValidPointRuleByUser(
            UUID userId,
            LocalDateTime date
    );

    Optional<PointRule> getValidPointRuleByMerchant(
            UUID merchantId,
            LocalDateTime date
    );
}