package com.fidely.backend.infrastructure.entities.mappers.merchants;

import com.fidely.backend.domain.models.Merchants.Merchant;
import com.fidely.backend.infrastructure.entities.models.merchants.MerchantEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre l'entité de persistance et le modèle de domaine
 * représentant un marchand.
 */
@Component
public class MerchantMapper {

    /**
     * Convertit une entité JPA en modèle de domaine.
     *
     * @param entity entité du marchand
     * @return marchand du domaine
     */
    public Merchant toDomain(MerchantEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Merchant entity cannot be null"
            );
        }

        return new Merchant(
                entity.getId(),
                null,
                entity.getName(),
                entity.getSlug(),
                entity.getLogoUrl(),
                entity.getPrimaryColor(),
                entity.getSecondaryColor(),
                entity.getDescription(),
                entity.getGoogleReviewUrl(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Convertit un modèle de domaine en entité JPA.
     *
     * @param merchant marchand du domaine
     * @return entité du marchand
     */
    public MerchantEntity toEntity(Merchant merchant) {
        if (merchant == null) {
            throw new IllegalArgumentException(
                    "Merchant cannot be null"
            );
        }

        return new MerchantEntity(
                merchant.getId(),
                merchant.getName(),
                merchant.getSlug(),
                merchant.getLogoUrl(),
                merchant.getPrimaryColor(),
                merchant.getSecondaryColor(),
                merchant.getDescription(),
                merchant.getGoogleReviewUrl(),
                merchant.isActive(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
        );
    }
}