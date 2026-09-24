package com.fidely.backend.infrastructure.entities.mappers.loyalties;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.infrastructure.entities.models.loyalties.LoyaltyEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le domaine Loyalty et son entité de persistance.
 */
@Component
public class LoyaltyMapper {

    /**
     * Convertit une entité de fidélité en modèle de domaine.
     *
     * @param entity entité à convertir
     * @return modèle de domaine correspondant
     */
    public Loyalty toDomain(LoyaltyEntity entity) {
        return new Loyalty(
                entity.getId(),
                entity.getCustomerId(),
                entity.getMerchantId(),
                entity.getPointsBalance(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Convertit un modèle de domaine en entité de persistance.
     *
     * @param domain modèle de domaine à convertir
     * @return entité correspondante
     */
    public LoyaltyEntity toEntity(Loyalty domain) {
        return new LoyaltyEntity(
                domain.getId(),
                domain.getCustomerId(),
                domain.getMerchantId(),
                domain.getPointsBalance(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}