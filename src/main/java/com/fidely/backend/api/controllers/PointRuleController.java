package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.models.loyalties.rewards.PointRuleResponse;
import com.fidely.backend.api.dtos.requests.loyalties.rewards.UpdatePointRuleRequest;
import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller REST permettant de gérer la règle d'attribution
 * de points du marchand connecté.
 */
@RestController
@RequestMapping("/merchants/me/points-rule")
public class PointRuleController {

    private final IPointRuleService pointRuleService;
    private final IUserRepository userRepository;

    /**
     * Crée un nouveau controller de gestion des règles de points.
     *
     * @param pointRuleService service des règles de points
     * @param userRepository repository des utilisateurs
     */
    public PointRuleController(
            IPointRuleService pointRuleService,
            IUserRepository userRepository
    ) {
        this.pointRuleService = pointRuleService;
        this.userRepository = userRepository;
    }

    /**
     * Récupère la règle actuellement applicable au marchand connecté.
     *
     * @param authentication authentification courante
     * @return règle de points actuellement applicable
     */
    @GetMapping
    public ResponseEntity<PointRuleResponse> getCurrentPointRule(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID merchantId = getMerchantId(userId);

        return pointRuleService
                .getValidPointRule(merchantId, LocalDateTime.now())
                .map(pointRule -> ResponseEntity.ok(toResponse(pointRule)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle règle de points pour le marchand connecté.
     *
     * @param request données de la nouvelle règle
     * @param authentication authentification courante
     * @return règle enregistrée
     */
    @PutMapping
    public ResponseEntity<PointRuleResponse> updatePointRule(
            @RequestBody UpdatePointRuleRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID merchantId = getMerchantId(userId);

        PointRule pointRule = new PointRule(
                UUID.randomUUID(),
                request.pointsPerCurrencyUnit(),
                request.roundingMethod(),
                request.active(),
                request.validFrom(),
                request.validTo(),
                LocalDateTime.now()
        );

        PointRule savedPointRule = pointRuleService.createPointRule(
                pointRule,
                merchantId
        );

        return ResponseEntity.ok(toResponse(savedPointRule));
    }

    /**
     * Récupère l'identifiant du marchand associé à un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return identifiant du marchand
     */
    private UUID getMerchantId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (!(user instanceof MerchantManager merchantManager)) {
            throw new IllegalArgumentException(
                    "User is not a merchant manager"
            );
        }

        return merchantManager.getMerchantId();
    }

    /**
     * Convertit une règle du domaine en réponse API.
     *
     * @param pointRule règle du domaine
     * @return réponse API
     */
    private PointRuleResponse toResponse(PointRule pointRule) {
        return new PointRuleResponse(
                pointRule.getId(),
                pointRule.getPointsPerCurrencyUnit(),
                pointRule.getRoundingMethod(),
                pointRule.isActive(),
                pointRule.getValidFrom(),
                pointRule.getValidTo(),
                pointRule.getCreatedAt()
        );
    }
}