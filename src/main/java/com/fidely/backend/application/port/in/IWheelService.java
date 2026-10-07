package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelPrize;

import java.util.List;
import java.util.UUID;

/**
 * Port d'entrée permettant de gérer les roues de récompenses.
 */
public interface IWheelService {

    /**
     * Récupère la roue associée à un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return roue du marchand
     */
    Wheel getWheelByMerchantId(UUID merchantId);

    /**
     * Récupère la roue du marchand associé au manager connecté.
     *
     * @param userId identifiant du manager
     * @return roue du marchand
     */
    Wheel getWheelForManager(UUID userId);

    /**
     * Crée ou met à jour la roue du marchand associé au manager.
     *
     * @param userId identifiant du manager
     * @param name nom de la roue
     * @param active indique si la roue est active
     * @param minIntervalMinutes intervalle minimal entre deux participations
     * @param requiresValidatedPurchase indique si un achat validé est requis
     * @return roue enregistrée
     */
    Wheel updateWheelForManager(
            UUID userId,
            String name,
            boolean active,
            int minIntervalMinutes,
            boolean requiresValidatedPurchase
    );

    /**
     * Récupère les lots associés à une roue.
     *
     * @param wheelId identifiant de la roue
     * @return liste des lots
     */
    List<WheelPrize> getPrizes(UUID wheelId);

    /**
     * Enregistre un lot pour le marchand associé au manager.
     *
     * @param userId identifiant du manager
     * @param label nom du lot
     * @param probabilityWeight taux de chance
     * @return lot enregistré
     */
    WheelPrize savePrizeForManager(UUID userId,
                                   String label,
                                   int probabilityWeight);

    /**
     * Modifie un lot appartenant à la roue du marchand connecté.
     *
     * @param userId identifiant de l'utilisateur connecté
     * @param prizeId identifiant du lot à modifier
     * @param label nouveau libellé du lot
     * @param probabilityWeight nouveau poids utilisé pour le tirage aléatoire
     * @return le lot modifié et sauvegardé
     */
    WheelPrize updatePrizeForManager(
            UUID userId,
            UUID prizeId,
            String label,
            int probabilityWeight
    );

    /**
     * Supprime un lot du marchand associé au manager.
     *
     * @param userId identifiant du manager
     * @param prizeId identifiant du lot
     */
    void deletePrizeForManager(UUID userId, UUID prizeId);

    /**
     * Effectue un tirage aléatoire sur une roue.
     *
     * @param merchantId identifiant du commerce
     * @return lot sélectionné
     */
    WheelPrize spin(UUID merchantId);
}