package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.models.wheels.*;
import com.fidely.backend.application.port.in.IWheelService;
import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelPrize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST permettant de gérer les roues de récompenses.
 */
@RestController
@RequestMapping("/merchants")
public class WheelController {

    private final IWheelService wheelService;

    /**
     * Crée un nouveau controller de gestion des roues.
     *
     * @param wheelService service métier des roues
     */
    public WheelController(IWheelService wheelService) {
        this.wheelService = wheelService;
    }

    /**
     * Récupère la roue publique d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return configuration de la roue
     */
    @GetMapping("/{merchantId}/wheel")
    public ResponseEntity<WheelResponse> getMerchantWheel(
            @PathVariable UUID merchantId
    ) {
        Wheel wheel = wheelService.getWheelByMerchantId(merchantId);

        return ResponseEntity.ok(toResponse(wheel));
    }

    /**
     * Récupère la roue du marchand connecté.
     *
     * @param authentication authentification courante
     * @return configuration de la roue
     */
    @GetMapping("/me/wheel")
    public ResponseEntity<WheelResponse> getCurrentMerchantWheel(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        Wheel wheel = wheelService.getWheelForManager(userId);

        return ResponseEntity.ok(toResponse(wheel));
    }

    /**
     * Crée ou modifie la roue du marchand connecté.
     *
     * @param request données de la roue
     * @param authentication authentification courante
     * @return roue enregistrée
     */
    @PutMapping("/me/wheel")
    public ResponseEntity<WheelResponse> updateCurrentMerchantWheel(
            @RequestBody UpdateWheelRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        Wheel wheel = wheelService.updateWheelForManager(
                userId,
                request.name(),
                request.active(),
                request.minIntervalMinutes(),
                request.requiresValidatedPurchase()
        );

        return ResponseEntity.ok(toResponse(wheel));
    }

    /**
     * Crée un nouveau lot pour la roue du marchand connecté.
     *
     * @param request informations du lot à créer
     * @param authentication authentification du marchand connecté
     * @return le lot créé
     */
    @PostMapping("/me/wheel/prizes")
    public ResponseEntity<WheelPrizeResponse> createPrize(
            @RequestBody UpdateWheelPrizeRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        Wheel wheel = wheelService.getWheelForManager(userId);

        WheelPrize prize = new WheelPrize(
                UUID.randomUUID(),
                wheel.getId(),
                request.label(),
                request.probabilityWeight(),
                LocalDateTime.now()
        );

        WheelPrize savedPrize = wheelService.savePrizeForManager(userId, prize);

        return ResponseEntity.ok(toPrizeResponse(savedPrize));
    }

    /**
     * Modifie un lot appartenant à la roue du marchand connecté.
     *
     * @param prizeId identifiant du lot à modifier
     * @param request nouvelles informations du lot
     * @param authentication authentification du marchand connecté
     * @return le lot modifié
     */
    @PatchMapping("/me/wheel/prizes/{prizeId}")
    public ResponseEntity<WheelPrizeResponse> updatePrize(
            @PathVariable UUID prizeId,
            @RequestBody UpdateWheelPrizeRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        Wheel wheel = wheelService.getWheelForManager(userId);

        WheelPrize existingPrize = wheelService.getPrizes(wheel.getId())
                .stream()
                .filter(prize -> prize.getId().equals(prizeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prize not found"));

        WheelPrize updatedPrize = new WheelPrize(
                existingPrize.getId(),
                existingPrize.getWheelId(),
                request.label(),
                request.probabilityWeight(),
                existingPrize.getCreatedAt()
        );

        WheelPrize savedPrize = wheelService.savePrizeForManager(
                userId,
                updatedPrize
        );

        return ResponseEntity.ok(toPrizeResponse(savedPrize));
    }

    /**
     * Supprime un lot appartenant à la roue du marchand connecté.
     *
     * @param prizeId identifiant du lot à supprimer
     * @param authentication authentification du marchand connecté
     * @return une réponse sans contenu
     */
    @DeleteMapping("/me/wheel/prizes/{prizeId}")
    public ResponseEntity<Void> deletePrize(
            @PathVariable UUID prizeId,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        wheelService.deletePrizeForManager(userId, prizeId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Effectue un tirage aléatoire sur la roue d'un marchand.
     *
     * <p>Cet endpoint est public et ne nécessite pas d'authentification.
     * Le résultat du tirage est calculé côté serveur.</p>
     *
     * @param merchantId identifiant du marchand
     * @return résultat du tirage
     */
    @PostMapping("/{merchantId}/wheel/spin")
    public ResponseEntity<SpinWheelResponse> spinWheel(
            @PathVariable UUID merchantId
    ) {
        Wheel wheel = wheelService.getWheelByMerchantId(merchantId);
        WheelPrize prize = wheelService.spin(wheel.getId());

        return ResponseEntity.ok(
                new SpinWheelResponse(prize.getLabel())
        );
    }

    /**
     * Convertit une roue du domaine en réponse API.
     *
     * @param wheel roue du domaine
     * @return réponse API
     */
    private WheelResponse toResponse(Wheel wheel) {
        List<WheelPrizeResponse> prizes = wheelService.getPrizes(wheel.getId())
                .stream()
                .map(this::toPrizeResponse)
                .toList();

        return new WheelResponse(
                wheel.getId(),
                wheel.getMerchantId(),
                wheel.getName(),
                wheel.isActive(),
                wheel.getMinIntervalMinutes(),
                wheel.isRequiresValidatedPurchase(),
                wheel.getCreatedAt(),
                prizes
        );
    }

    /**
     * Convertit un lot du domaine en réponse API.
     *
     * @param prize lot du domaine
     * @return réponse API
     */
    private WheelPrizeResponse toPrizeResponse(WheelPrize prize) {
        return new WheelPrizeResponse(
                prize.getId(),
                prize.getWheelId(),
                prize.getLabel(),
                prize.getProbabilityWeight(),
                prize.getCreatedAt()
        );
    }
}