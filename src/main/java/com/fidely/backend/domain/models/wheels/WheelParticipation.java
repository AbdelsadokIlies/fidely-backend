package com.fidely.backend.domain.models.wheels;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente la participation d'un client à une roue de récompenses.
 *
 * <p>Une participation est associée à une roue, à un client et, lorsqu'il
 * est requis, au ticket ayant permis la participation. Elle conserve
 * également le résultat obtenu et la date de participation.</p>
 */
public class WheelParticipation {

    private final UUID id;
    private final UUID wheelId;
    private final UUID customerId;
    private final UUID ticketId;
    private final WheelPrize wheelPrize;
    private final String resultLabel;
    private final LocalDateTime playedAt;

    /**

     * Crée une nouvelle participation à une roue de récompenses.
     *
     * @param id identifiant de la participation
     * @param wheelId id de la roue à laquelle le client a participé
     * @param customerId id du client ayant participé
     * @param ticketId id du ticket associé à la participation
     * @param wheelPrize lot obtenu lors de la participation
     * @param resultLabel libellé du résultat obtenu
     * @param playedAt date et heure de la participation
     */
    public WheelParticipation(
            UUID id,
            UUID wheelId,
            UUID customerId,
            UUID ticketId,
            WheelPrize wheelPrize,
            String resultLabel,
            LocalDateTime playedAt
    ) {
        this.id = id;
        this.wheelId = wheelId;
        this.customerId = customerId;
        this.ticketId = ticketId;
        this.wheelPrize = wheelPrize;
        this.resultLabel = resultLabel;
        this.playedAt = playedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWheelId() {
        return wheelId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public WheelPrize getWheelPrize() {
        return wheelPrize;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }
}
