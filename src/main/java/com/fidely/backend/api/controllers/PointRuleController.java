package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.models.loyalties.rewards.PointRuleResponse;
import com.fidely.backend.api.dtos.models.loyalties.rewards.UpdatePointRuleRequest;
import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller REST permettant de gérer la règle d'attribution
 * de points du marchand connecté.
 */
@RestController
@RequestMapping("/merchants")
public class PointRuleController {

    private final IPointRuleService pointRuleService;

    /**
     * Crée un nouveau controller de gestion des règles de points.
     *
     * @param pointRuleService service des règles de points
     */
    public PointRuleController(
            IPointRuleService pointRuleService
    ) {
        this.pointRuleService = pointRuleService;
    }

    /**
     * Récupère la règle actuellement applicable au marchand connecté.
     *
     * @param authentication authentification courante
     * @return règle de points actuellement applicable
     */
    @GetMapping("/me/points-rule")
    @PreAuthorize("hasRole('MERCHANT_MANAGER')")
    public ResponseEntity<PointRuleResponse> getCurrentPointRule(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        return pointRuleService
                .getValidPointRuleByUser(userId, LocalDateTime.now())
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
    @PutMapping("/me/points-rule")
    @PreAuthorize("hasRole('MERCHANT_MANAGER')")
    public ResponseEntity<PointRuleResponse> updatePointRule(
            @Valid @RequestBody UpdatePointRuleRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        PointRule savedPointRule = pointRuleService.createPointRule(
                userId,
                request.pointsPerCurrencyUnit(),
                request.roundingMethod(),
                request.active(),
                request.validFrom(),
                request.validTo(),
                LocalDateTime.now()
        );

        return ResponseEntity.ok(toResponse(savedPointRule));
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