package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IRewardRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import com.fidely.backend.infrastructure.entities.models.loyalties.Rewards.RewardEntity;
import com.fidely.backend.infrastructure.mappers.loyalties.Rewards.RewardMapper;
import com.fidely.backend.infrastructure.repositories.loyalties.Rewards.SpringDataRewardRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur permettant au domaine d'utiliser le repository Spring Data.
 */
@Repository
public class RewardRepositoryAdapter implements IRewardRepository {

    private final SpringDataRewardRepository repository;
    private final RewardMapper mapper;

    /**
     * Crée un nouvel adaptateur de repository.
     *
     * @param repository repository Spring Data
     * @param mapper mapper des récompenses
     */
    public RewardRepositoryAdapter(
            SpringDataRewardRepository repository,
            RewardMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Recherche une récompense par son identifiant.
     *
     * @param rewardId identifiant de la récompense
     * @return récompense trouvée ou vide
     */
    @Override
    public Optional<Reward> findById(UUID rewardId) {
        if (rewardId == null) {
            throw new IllegalArgumentException(
                    "Reward id cannot be null"
            );
        }

        return repository.findById(rewardId)
                .map(mapper::toDomain);
    }

    /**
     * Recherche les récompenses d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des récompenses
     */
    @Override
    public List<Reward> findByMerchantId(UUID merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException(
                    "Merchant id cannot be null"
            );
        }

        return repository.findByMerchantId(merchantId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Sauvegarde une récompense.
     *
     * @param reward récompense à sauvegarder
     * @return récompense sauvegardée
     */
    @Override
    public Reward save(Reward reward) {
        if (reward == null) {
            throw new IllegalArgumentException(
                    "Reward cannot be null"
            );
        }

        RewardEntity entity = mapper.toEntity(reward);
        RewardEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    /**
     * Supprime une récompense par son identifiant.
     *
     * @param rewardId identifiant de la récompense
     */
    @Override
    public void deleteById(UUID rewardId) {
        if (rewardId == null) {
            throw new IllegalArgumentException(
                    "Reward id cannot be null"
            );
        }

        repository.deleteById(rewardId);
    }
}