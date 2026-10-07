package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.loyalties.Rewards.Reward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant de gérer les récompenses de fidélité.
 */
public interface IRewardRepository {

    /**
     * Recherche une récompense par son identifiant.
     *
     * @param rewardId identifiant de la récompense
     * @return récompense trouvée ou vide
     */
    Optional<Reward> findById(UUID rewardId);

    /**
     * Recherche les récompenses d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des récompenses
     */
    List<Reward> findByMerchantId(UUID merchantId);

    /**
     * Sauvegarde une récompense.
     *
     * @param reward récompense à sauvegarder
     * @return récompense sauvegardée
     */
    Reward save(Reward reward);

    /**
     * Supprime une récompense.
     *
     * @param rewardId identifiant de la récompense
     */
    void deleteById(UUID rewardId);
}