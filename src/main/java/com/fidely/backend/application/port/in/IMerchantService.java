package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.Merchants.Merchant;

import java.util.UUID;

/**
 * Port d'entrée permettant de gérer les opérations liées aux marchands.
 */
public interface IMerchantService {

    /**
     * Récupère le marchand associé à un gestionnaire connecté.
     *
     * @param userId identifiant de l'utilisateur connecté
     * @return marchand associé
     */
    Merchant getMerchantForManager(UUID userId);

    /**
     * Met à jour les informations générales d'un marchand.
     *
     * @param userId identifiant de l'utilisateur connecté
     * @param name nouveau nom du marchand
     * @param slug nouveau slug du marchand
     * @param description nouvelle description du marchand
     * @param googleReviewUrl nouvelle URL des avis Google
     * @return marchand mis à jour
     */
    Merchant updateMerchant(
            UUID userId,
            String name,
            String slug,
            String description,
            String googleReviewUrl
    );

    /**
     * Met à jour le branding d'un marchand.
     *
     * @param userId identifiant de l'utilisateur connecté
     * @param logoUrl nouvelle URL du logo
     * @param primaryColor nouvelle couleur primaire
     * @param secondaryColor nouvelle couleur secondaire
     * @return marchand mis à jour
     */
    Merchant updateBranding(
            UUID userId,
            String logoUrl,
            String primaryColor,
            String secondaryColor
    );

    /**
     * Récupère les informations publiques d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return marchand demandé
     */
    Merchant getPublicMerchant(UUID merchantId);
}