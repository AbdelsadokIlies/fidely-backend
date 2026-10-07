package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.out.IPointRuleRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final IUserRepository userRepository;

    /**
     * Crée un nouveau service de gestion des règles de points.
     *
     * @param pointRuleRepository repository des règles de points
     */
    public PointRuleService(
            IPointRuleRepository pointRuleRepository,
            IUserRepository userRepository
    ) {
        this.pointRuleRepository = pointRuleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Enregistre une nouvelle règle de points pour un marchand.
     *
     * <p>Lorsqu'une nouvelle règle active est créée, la règle active
     * précédente est clôturée afin de conserver l'historique des règles.</p>
     *
     * @param userId identifiant de l'utilisateur
     * @param pointsPerCurrencyUnit nombre de points attribués par unité monétaire
     * @param roundingMethod méthode d'arrondi utilisée pour le calcul
     * @param active indique si la règle est active
     * @param validFrom date à partir de laquelle la règle est valide
     * @param validTo date jusqu'à laquelle la règle est valide, ou null sans date de fin
     * @param dateTime date de création de la règle
     * @return règle enregistrée
     */
    @Override
    public PointRule createPointRule(
            UUID userId,
            BigDecimal pointsPerCurrencyUnit,
            RoundingMethod roundingMethod,
            boolean active,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime dateTime
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId id cannot be null"
            );
        }

        UUID merchantId = userRepository.getMerchantId(userId);

        PointRule pointRule = new PointRule(UUID.randomUUID(),
                pointsPerCurrencyUnit,
                roundingMethod,
                active,
                validFrom,
                validTo,
                dateTime);

        if (pointRule.isActive()) {
            closeCurrentActiveRule(
                    merchantId,
                    pointRule.getValidFrom()
            );
        }

        return pointRuleRepository.save(
                pointRule,
                merchantId
        );
    }

    /**
     * Récupère toutes les règles de points d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return règles de points du marchand
     */
    @Override
    public List<PointRule> getPointRulesByMerchant(
            UUID merchantId
    ) {
        if (merchantId == null) {
            throw new IllegalArgumentException(
                    "Merchant id cannot be null"
            );
        }

        return pointRuleRepository.findByMerchantId(
                merchantId
        );
    }

    /**
     * Récupère la règle valide pour un marchand à une date donnée.
     *
     * @param userId identifiant de l'utilisateur
     * @param date date à vérifier
     * @return règle valide si elle existe
     */
    @Override
    public Optional<PointRule> getValidPointRule(
            UUID userId,
            LocalDateTime date
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "UserId id cannot be null"
            );
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "Date cannot be null"
            );
        }

        UUID merchantId = userRepository.getMerchantId(userId);

        return pointRuleRepository
                .findByMerchantId(merchantId)
                .stream()
                .filter(pointRule -> pointRule.isValidAt(date))
                .findFirst();
    }

    /**
     * Clôture la règle active actuelle d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @param newValidFrom date de début de la nouvelle règle
     */
    private void closeCurrentActiveRule(
            UUID merchantId,
            LocalDateTime newValidFrom
    ) {
        pointRuleRepository
                .findByMerchantId(merchantId)
                .stream()
                .filter(PointRule::isActive)
                .filter(pointRule ->
                        pointRule.getValidTo() == null
                                || pointRule.getValidTo().isAfter(newValidFrom)
                )
                .findFirst()
                .ifPresent(currentRule -> {
                    PointRule closedRule = new PointRule(
                            currentRule.getId(),
                            currentRule.getPointsPerCurrencyUnit(),
                            currentRule.getRoundingMethod(),
                            false,
                            currentRule.getValidFrom(),
                            newValidFrom,
                            currentRule.getCreatedAt()
                    );

                    pointRuleRepository.save(
                            closedRule,
                            merchantId
                    );
                });
    }
}