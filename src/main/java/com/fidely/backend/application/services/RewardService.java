package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IRewardService;
import com.fidely.backend.application.port.out.IRewardRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service applicatif permettant de gérer les récompenses de fidélité.
 */
@Service
public class RewardService implements IRewardService {

    private final IRewardRepository rewardRepository;
    private final IUserRepository userRepository;

    /**
     * Crée un nouveau service de gestion des récompenses.
     *
     * @param rewardRepository repository des récompenses
     */
    public RewardService(
            IRewardRepository rewardRepository,
            IUserRepository userRepository
    ) {
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crée une récompense.
     *
     * @param reward récompense à créer
     * @return récompense créée
     */
    @Override
    public Reward createReward(UUID userId, Reward reward) {
        if (reward == null) {
            throw new IllegalArgumentException(
                    "Reward cannot be null"
            );
        }

        reward.setMerchantId(userRepository.getMerchantId(userId));

        return rewardRepository.save(reward);
    }

    /**
     * Recherche une récompense par son identifiant.
     *
     * @param rewardId identifiant de la récompense
     * @return récompense trouvée ou vide
     */
    @Override
    public Optional<Reward> getRewardById(UUID rewardId) {
        if (rewardId == null) {
            throw new IllegalArgumentException(
                    "Reward id cannot be null"
            );
        }

        return rewardRepository.findById(rewardId);
    }

    /**
     * Recherche les récompenses d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des récompenses
     */
    @Override
    public List<Reward> getRewardsByMerchant(UUID merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException(
                    "Merchant id cannot be null"
            );
        }

        return rewardRepository.findByMerchantId(merchantId);
    }

    /**
     * Met à jour une récompense.
     *
     * @param reward récompense à mettre à jour
     * @return récompense mise à jour
     */
    @Override
    public Reward updateReward(Reward reward) {
        if (reward == null) {
            throw new IllegalArgumentException(
                    "Reward cannot be null"
            );
        }

        if (reward.getId() == null) {
            throw new IllegalArgumentException(
                    "Reward id cannot be null"
            );
        }

        if (rewardRepository.findById(reward.getId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Reward not found"
            );
        }

        return rewardRepository.save(reward);
    }

    /**
     * Supprime une récompense.
     *
     * @param rewardId identifiant de la récompense
     */
    @Override
    public void deleteReward(UUID rewardId) {
        if (rewardId == null) {
            throw new IllegalArgumentException(
                    "Reward id cannot be null"
            );
        }

        if (rewardRepository.findById(rewardId).isEmpty()) {
            throw new IllegalArgumentException(
                    "Reward not found"
            );
        }

        rewardRepository.deleteById(rewardId);
    }
}