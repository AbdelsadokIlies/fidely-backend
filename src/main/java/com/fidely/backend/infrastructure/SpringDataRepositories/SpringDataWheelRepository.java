package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.wheels.WheelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant d'accéder aux roues de récompenses.
 */
public interface SpringDataWheelRepository extends JpaRepository<WheelEntity, UUID> {

    /**
     * Recherche la roue associée à un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return la roue du marchand si elle existe
     */
    Optional<WheelEntity> findByMerchantId(UUID merchantId);
}