package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.loyalties.rewards.RewardResponseMapper;
import com.fidely.backend.api.dtos.mappers.loyalties.rewards.UpdateRewardRequestMapper;
import com.fidely.backend.api.dtos.mappers.rewards.CreateRewardRequestMapper;
import com.fidely.backend.api.dtos.models.loyalties.rewards.RewardResponse;
import com.fidely.backend.api.dtos.models.loyalties.rewards.UpdateRewardRequest;
import com.fidely.backend.api.dtos.models.rewards.CreateRewardRequest;
import com.fidely.backend.application.port.in.IRewardService;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import com.fidely.backend.domain.models.users.MerchantManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST permettant de gérer les récompenses de fidélité.
 */
@RestController
public class RewardController {

    private final IRewardService rewardService;
    private final CreateRewardRequestMapper createRewardRequestMapper;
    private final UpdateRewardRequestMapper updateRewardRequestMapper;
    private final RewardResponseMapper rewardResponseMapper;

    /**
     * Crée un nouveau controller de récompenses.
     *
     * @param rewardService service de gestion des récompenses
     * @param createRewardRequestMapper mapper des requêtes de création
     * @param updateRewardRequestMapper mapper des requêtes de modification
     * @param rewardResponseMapper mapper des réponses
     */
    public RewardController(
            IRewardService rewardService,
            CreateRewardRequestMapper createRewardRequestMapper,
            UpdateRewardRequestMapper updateRewardRequestMapper,
            RewardResponseMapper rewardResponseMapper
    ) {
        this.rewardService = rewardService;
        this.createRewardRequestMapper = createRewardRequestMapper;
        this.updateRewardRequestMapper = updateRewardRequestMapper;
        this.rewardResponseMapper = rewardResponseMapper;
    }

    /**
     * Récupère les récompenses publiques d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des récompenses
     */
    @GetMapping("/merchants/{merchantId}/rewards")
    public ResponseEntity<List<RewardResponse>> getMerchantRewards(
            @PathVariable UUID merchantId
    ) {
        List<RewardResponse> response =
                rewardService.getRewardsByMerchant(merchantId)
                        .stream()
                        .map(rewardResponseMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Crée une récompense pour le marchand authentifié.
     *
     * @param request requête de création
     * @return récompense créée
     */
    @PostMapping("/merchants/me/rewards")
    @PreAuthorize("hasRole('MERCHANT_MANAGER')")
    public ResponseEntity<RewardResponse> createReward(
            @RequestBody CreateRewardRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        Reward reward =
                createRewardRequestMapper.toDomain(
                        request
                );

        Reward createdReward = rewardService.createReward(userId, reward);

        return ResponseEntity.ok(
                rewardResponseMapper.toResponse(createdReward)
        );
    }

    /**
     * Modifie une récompense du marchand authentifié.
     *
     * @param rewardId identifiant de la récompense
     * @param request requête de modification
     * @return récompense modifiée
     */
    @PatchMapping("/merchants/me/rewards/{rewardId}")
    @PreAuthorize("hasRole('MERCHANT_MANAGER')")
    public ResponseEntity<RewardResponse> updateReward(
            @PathVariable UUID rewardId,
            @RequestBody UpdateRewardRequest request
    ) {

        Reward updatedReward =
                updateRewardRequestMapper.toDomain(
                        rewardId,
                        request
                );

        Reward savedReward =
                rewardService.updateReward(updatedReward);

        return ResponseEntity.ok(
                rewardResponseMapper.toResponse(savedReward)
        );
    }

    /**
     * Supprime une récompense du marchand authentifié.
     *
     * @param rewardId identifiant de la récompense
     * @return réponse sans contenu
     */
    @DeleteMapping("/merchants/me/rewards/{rewardId}")
    @PreAuthorize("hasRole('MERCHANT_MANAGER')")
    public ResponseEntity<Void> deleteReward(
            @PathVariable UUID rewardId
    ) {

        rewardService.deleteReward(rewardId);

        return ResponseEntity.noContent().build();
    }
}