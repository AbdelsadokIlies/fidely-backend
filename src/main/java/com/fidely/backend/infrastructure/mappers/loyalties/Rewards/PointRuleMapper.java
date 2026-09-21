package com.fidely.backend.infrastructure.mappers.loyalties.Rewards;

import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.infrastructure.entities.loyalties.Rewards.PointRuleEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre les règles de points du domaine et leurs entités JPA.
 */
@Component
public class PointRuleMapper {

    /**
     * Convertit une entité JPA en modèle du domaine.
     *
     * @param entity entité JPA à convertir
     * @return modèle de règle de points correspondant
     */
    public PointRule toDomain(PointRuleEntity entity) {
        return new PointRule(
                entity.getId(),
                entity.getPointsPerCurrencyUnit(),
                toRoundingMethod(entity.getRoundingMethod()),
                entity.isActive(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit un modèle du domaine en entité JPA.
     *
     * @param domain modèle de règle de points à convertir
     * @param merchantId identifiant du marchand associé
     * @return entité JPA correspondante
     */
    public PointRuleEntity toEntity(
            PointRule domain,
            java.util.UUID merchantId
    ) {
        return new PointRuleEntity(
                domain.getId(),
                merchantId,
                domain.getPointsPerCurrencyUnit(),
                toRoundingMethodValue(domain.getRoundingMethod()),
                domain.isActive(),
                domain.getValidFrom(),
                domain.getValidTo(),
                domain.getCreatedAt()
        );
    }

    private RoundingMethod toRoundingMethod(short value) {
        return switch (value) {
            case 0 -> RoundingMethod.FLOOR;
            case 1 -> RoundingMethod.ROUND;
            case 2 -> RoundingMethod.CEIL;
            default -> throw new IllegalArgumentException(
                    "Unknown rounding method value: " + value
            );
        };
    }

    private short toRoundingMethodValue(
            RoundingMethod roundingMethod
    ) {
        return switch (roundingMethod) {
            case FLOOR -> 0;
            case ROUND -> 1;
            case CEIL -> 2;
        };
    }
}