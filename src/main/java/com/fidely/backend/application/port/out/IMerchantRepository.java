package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.Merchants.Merchant;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant l'accès aux marchands.
 *
 * <p>Ce port abstrait l'accès à la persistance des marchands
 * pour la couche applicative.</p>
 */
public interface IMerchantRepository {

    /**
     * Recherche un marchand à partir de son identifiant.
     *
     * @param merchantId identifiant du marchand
     * @return le marchand s'il existe
     */
    Optional<Merchant> findById(UUID merchantId);

    /**
     * Enregistre un marchand.
     *
     * @param merchant marchand à enregistrer
     * @return marchand enregistré
     */
    Merchant save(Merchant merchant);
}