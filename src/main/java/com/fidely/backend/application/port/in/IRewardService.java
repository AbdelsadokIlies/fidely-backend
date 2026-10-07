package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.loyalties.Rewards.Reward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrée permettant de gérer les récompenses de fidélité.
 */
public interface IRewardService {

    /**
     * Crée une récompense pour un marchand.
     *
     * @param reward récompense à créer
     * @return récompense créée
     */
    Reward createReward(UUID userId, Reward reward);

    /**
     * Recherche une récompense par son identifiant.
     *
     * @param rewardId identifiant de la récompense
     * @return récompense trouvée ou vide
     */
    Optional<Reward> getRewardById(UUID rewardId);

    /**
     * Recherche toutes les récompenses d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des récompenses
     */
    List<Reward> getRewardsByMerchant(UUID merchantId);

    /**
     * Met à jour une récompense.
     *
     * @param reward récompense contenant les nouvelles données
     * @return récompense mise à jour
     */
    Reward updateReward(Reward reward);

    /**
     * Supprime une récompense.
     *
     * @param rewardId identifiant de la récompense
     */
    void deleteReward(UUID rewardId);
}