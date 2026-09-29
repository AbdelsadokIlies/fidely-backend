package com.fidely.backend.infrastructure.entities.mappers.wheels;

import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.infrastructure.entities.models.wheels.WheelEntity;

/**
 * Mapper entre le domaine {@link Wheel} et l'entité JPA {@link WheelEntity}.
 */
public class WheelMapper {

    /**
     * Convertit une entité JPA en objet du domaine.
     *
     * @param entity entité JPA représentant la roue
     * @return roue du domaine
     */
    public Wheel toDomain(WheelEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Wheel(
                entity.getId(),
                entity.getMerchantId(),
                entity.getName(),
                entity.isActive(),
                entity.getMinIntervalMinutes(),
                entity.isRequiresValidatedPurchase(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit un objet du domaine en entité JPA.
     *
     * @param domain roue du domaine
     * @return entité JPA
     */
    public WheelEntity toEntity(Wheel domain) {
        if (domain == null) {
            return null;
        }

        return new WheelEntity(
                domain.getId(),
                domain.getMerchantId(),
                domain.getName(),
                domain.isActive(),
                domain.getMinIntervalMinutes(),
                domain.isRequiresValidatedPurchase(),
                domain.getCreatedAt()
        );
    }
}