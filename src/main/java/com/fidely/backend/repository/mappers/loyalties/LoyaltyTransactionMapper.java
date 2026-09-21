package com.fidely.backend.repository.mappers.loyalties;

import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.repository.entities.loyalties.LoyaltyTransactionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine LoyaltyTransaction
 * et son entité de persistance.
 */
@Component
public class LoyaltyTransactionMapper {

    /**
     * Convertit une entité de transaction en modèle de domaine.
     *
     * @param entity entité à convertir
     * @return modèle de domaine correspondant
     */
    public LoyaltyTransaction toDomain(
            LoyaltyTransactionEntity entity
    ) {
        return new LoyaltyTransaction(
                entity.getId(),
                entity.getLoyaltyId(),
                entity.getTicketId(),
                entity.getPoints(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit un modèle de domaine en entité de persistance.
     *
     * @param domain modèle de domaine à convertir
     * @return entité correspondante
     */
    public LoyaltyTransactionEntity toEntity(
            LoyaltyTransaction domain
    ) {
        return new LoyaltyTransactionEntity(
                domain.getId(),
                domain.getLoyaltyId(),
                domain.getTicketId(),
                domain.getPoints(),
                domain.getDescription(),
                domain.getCreatedAt()
        );
    }
}