package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux programmes de fidélité.
 */
public interface SpringDataLoyaltyRepository
        extends JpaRepository<LoyaltyEntity, UUID> {

    Optional<LoyaltyEntity> findByCustomerIdAndMerchantId(
            UUID customerId,
            UUID merchantId
    );

    List<LoyaltyEntity> findByCustomerId(UUID customerId);

    List<LoyaltyEntity> findByMerchantId(UUID merchantId);
}