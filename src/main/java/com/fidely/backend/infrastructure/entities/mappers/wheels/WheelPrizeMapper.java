package com.fidely.backend.infrastructure.entities.mappers.wheels;

import com.fidely.backend.domain.models.wheels.WheelPrize;
import com.fidely.backend.infrastructure.entities.models.wheels.WheelPrizeEntity;

/**
 * Mapper entre le domaine {@link WheelPrize} et l'entité JPA {@link WheelPrizeEntity}.
 */
public class WheelPrizeMapper {

    /**
     * Convertit une entité JPA en objet du domaine.
     *
     * @param entity entité JPA représentant le lot
     * @return lot du domaine
     */
    public WheelPrize toDomain(WheelPrizeEntity entity) {
        if (entity == null) {
            return null;
        }

        return new WheelPrize(
                entity.getId(),
                entity.getWheelId(),
                entity.getLabel(),
                entity.getProbabilityWeight(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit un objet du domaine en entité JPA.
     *
     * @param domain lot du domaine
     * @return entité JPA
     */
    public WheelPrizeEntity toEntity(WheelPrize domain) {
        if (domain == null) {
            return null;
        }

        return new WheelPrizeEntity(
                domain.getId(),
                domain.getWheelId(),
                domain.getLabel(),
                domain.getProbabilityWeight(),
                domain.getCreatedAt()
        );
    }
}