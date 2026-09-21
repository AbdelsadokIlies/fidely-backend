package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IPointRuleRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataPointRuleRepository;
import com.fidely.backend.infrastructure.entities.loyalties.Rewards.PointRuleEntity;
import com.fidely.backend.infrastructure.mappers.loyalties.Rewards.PointRuleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur permettant d'utiliser le repository Spring Data
 * à travers le port de sortie de gestion des règles de points.
 */
@Repository
public class PointRuleRepositoryAdapter
        implements IPointRuleRepository {

    private final SpringDataPointRuleRepository pointRuleRepository;
    private final PointRuleMapper pointRuleMapper;

    public PointRuleRepositoryAdapter(
            SpringDataPointRuleRepository pointRuleRepository,
            PointRuleMapper pointRuleMapper
    ) {
        this.pointRuleRepository = pointRuleRepository;
        this.pointRuleMapper = pointRuleMapper;
    }

    @Override
    public Optional<PointRule> findById(UUID id) {
        return pointRuleRepository.findById(id)
                .map(pointRuleMapper::toDomain);
    }

    @Override
    public List<PointRule> findByMerchantId(UUID merchantId) {
        return pointRuleRepository.findByMerchantId(merchantId)
                .stream()
                .map(pointRuleMapper::toDomain)
                .toList();
    }

    @Override
    public PointRule save(
            PointRule pointRule,
            UUID merchantId
    ) {
        PointRuleEntity entity =
                pointRuleMapper.toEntity(
                        pointRule,
                        merchantId
                );

        PointRuleEntity savedEntity =
                pointRuleRepository.save(entity);

        return pointRuleMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        pointRuleRepository.deleteById(id);
    }
}