package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux transactions de points.
 */
public interface SpringDataLoyaltyTransactionRepository
        extends JpaRepository<LoyaltyTransactionEntity, UUID> {

    List<LoyaltyTransactionEntity> findByLoyaltyId(
            UUID loyaltyId
    );

    List<LoyaltyTransactionEntity> findByTicketId(
            UUID ticketId
    );
}