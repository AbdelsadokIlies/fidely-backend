package com.fidely.backend.infrastructure.entities.mappers.wheels;

import com.fidely.backend.domain.models.wheels.WheelParticipation;
import com.fidely.backend.domain.models.wheels.WheelPrize;
import com.fidely.backend.infrastructure.entities.models.wheels.WheelParticipationEntity;

/**
 * Mapper entre le domaine {@link WheelParticipation}
 * et l'entité JPA {@link WheelParticipationEntity}.
 */
public class WheelParticipationMapper {

    /**
     * Convertit une entité JPA en objet du domaine.
     *
     * @param entity entité JPA représentant la participation
     * @param wheelPrize lot associé à la participation
     * @return participation du domaine
     */
    public WheelParticipation toDomain(
            WheelParticipationEntity entity,
            WheelPrize wheelPrize
    ) {
        if (entity == null) {
            return null;
        }

        return new WheelParticipation(
                entity.getId(),
                entity.getWheelId(),
                entity.getCustomerId(),
                entity.getTicketId(),
                wheelPrize,
                entity.getResultLabel(),
                entity.getPlayedAt()
        );
    }

    /**
     * Convertit un objet du domaine en entité JPA.
     *
     * @param domain participation du domaine
     * @return entité JPA
     */
    public WheelParticipationEntity toEntity(WheelParticipation domain) {
        if (domain == null) {
            return null;
        }

        WheelPrize wheelPrize = domain.getWheelPrize();

        return new WheelParticipationEntity(
                domain.getId(),
                domain.getWheelId(),
                domain.getCustomerId(),
                domain.getTicketId(),
                wheelPrize != null ? wheelPrize.getId() : null,
                domain.getResultLabel(),
                domain.getPlayedAt()
        );
    }
}