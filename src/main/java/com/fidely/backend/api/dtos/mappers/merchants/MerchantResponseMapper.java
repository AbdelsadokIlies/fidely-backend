package com.fidely.backend.api.dtos.mappers.merchants;

import com.fidely.backend.api.dtos.models.merchants.MerchantResponse;
import com.fidely.backend.domain.models.Merchants.Merchant;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine Merchant
 * et sa représentation API.
 */
@Component
public class MerchantResponseMapper {

    public MerchantResponse toResponse(Merchant merchant) {
        if (merchant == null) {
            throw new IllegalArgumentException(
                    "Merchant cannot be null"
            );
        }

        return new MerchantResponse(
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