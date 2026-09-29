package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelParticipation;
import com.fidely.backend.domain.models.wheels.WheelPrize;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant d'accéder aux données liées aux roues
 * de récompenses, à leurs lots et à leurs participations.
 */
public interface IWheelRepository {

    /**
     * Récupère une roue à partir de son identifiant.
     *
     * @param wheelId identifiant de la roue
     * @return la roue si elle existe
     */
    Optional<Wheel> findById(UUID wheelId);

    /**
     * Récupère la roue associée à un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return la roue du marchand si elle existe
     */
    Optional<Wheel> findByMerchantId(UUID merchantId);

    /**
     * Enregistre une roue.
     *
     * @param wheel roue à enregistrer
     * @return la roue enregistrée
     */
    Wheel saveWheel(Wheel wheel);

    /**
     * Récupère tous les lots associés à une roue.
     *
     * @param wheelId identifiant de la roue
     * @return liste des lots de la roue
     */
    List<WheelPrize> findPrizesByWheelId(UUID wheelId);

    /**
     * Récupère un lot à partir de son identifiant.
     *
     * @param prizeId identifiant du lot
     * @return le lot si celui-ci existe
     */
    Optional<WheelPrize> findPrizeById(UUID prizeId);

    /**
     * Enregistre un lot.
     *
     * @param prize lot à enregistrer
     * @return le lot enregistré
     */
    WheelPrize savePrize(WheelPrize prize);

    /**
     * Supprime un lot à partir de son identifiant.
     *
     * @param prizeId identifiant du lot à supprimer
     */
    void deletePrizeById(UUID prizeId);

    /**
     * Récupère une participation à partir de son identifiant.
     *
     * @param participationId identifiant de la participation
     * @return la participation si elle existe
     */
    Optional<WheelParticipation> findParticipationById(UUID participationId);

    /**
     * Récupère les participations d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des participations du client
     */
    List<WheelParticipation> findParticipationsByCustomerId(UUID customerId);

    /**
     * Récupère la dernière participation d'un client à une roue.
     *
     * <p>Cette méthode permet notamment de vérifier l'intervalle minimal
     * entre deux participations.</p>
     *
     * @param customerId identifiant du client
     * @param wheelId identifiant de la roue
     * @return la dernière participation si elle existe
     */
    Optional<WheelParticipation> findLatestParticipation(
            UUID customerId,
            UUID wheelId
    );

    /**
     * Enregistre une participation.
     *
     * @param participation participation à enregistrer
     * @return la participation enregistrée
     */
    WheelParticipation saveParticipation(WheelParticipation participation);
}