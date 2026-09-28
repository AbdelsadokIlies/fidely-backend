package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.merchants.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux marchands.
 */
public interface SpringDataMerchantRepository
        extends JpaRepository<MerchantEntity, UUID> {
}