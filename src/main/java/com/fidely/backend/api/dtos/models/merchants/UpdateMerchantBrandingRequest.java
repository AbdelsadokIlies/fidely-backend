package com.fidely.backend.api.dtos.models.merchants;

/**
 * Données permettant de mettre à jour le branding d'un marchand.
 *
 * @param logoUrl nouvelle URL du logo
 * @param primaryColor nouvelle couleur primaire
 * @param secondaryColor nouvelle couleur secondaire
 */
public record UpdateMerchantBrandingRequest(
        String logoUrl,
        String primaryColor,
        String secondaryColor
) {
}