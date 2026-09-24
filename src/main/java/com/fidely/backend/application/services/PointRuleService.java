package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.out.IPointRuleRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service applicatif permettant de gérer les règles d'attribution de points.
 */
@Service
public class PointRuleService implements IPointRuleService {

    private final IPointRuleRepository pointRuleRepository;

    public PointRuleService(
            IPointRuleRepository pointRuleRepository
    ) {
        this.pointRuleRepository = pointRuleRepository;
    }

    @Override
    public PointRule createPointRule(
            PointRule pointRule,
            UUID merchantId
    ) {
        return pointRuleRepository.save(
                pointRule,
                merchantId
        );
    }

    @Override
    public Optional<PointRule> getPointRuleById(
            UUID pointRuleId
    ) {
        return pointRuleRepository.findById(pointRuleId);
    }

    @Override
    public List<PointRule> getPointRulesByMerchant(
            UUID merchantId
    ) {
        return pointRuleRepository.findByMerchantId(
                merchantId
        );
    }

    @Override
    public Optional<PointRule> getValidPointRule(
            UUID merchantId,
            LocalDateTime date
    ) {
        return pointRuleRepository
                .findByMerchantId(merchantId)
                .stream()
                .filter(pointRule -> pointRule.isValidAt(date))
                .findFirst();
    }

    @Override
    public void deletePointRule(UUID pointRuleId) {
        pointRuleRepository.deleteById(pointRuleId);
    }
}