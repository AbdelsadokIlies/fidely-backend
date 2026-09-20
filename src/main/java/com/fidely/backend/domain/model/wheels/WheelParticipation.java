package com.fidely.backend.domain.model.wheels;

import com.fidely.backend.domain.model.tickets.Ticket;
import com.fidely.backend.domain.model.users.Customer;

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
    private final Wheel wheel;
    private final Customer customer;
    private final Ticket ticket;
    private final WheelPrize wheelPrize;
    private final String resultLabel;
    private final LocalDateTime playedAt;

    /**

     * Crée une nouvelle participation à une roue de récompenses.
     *
     * @param id identifiant de la participation
     * @param wheel roue à laquelle le client a participé
     * @param customer client ayant participé
     * @param ticket ticket associé à la participation
     * @param wheelPrize lot obtenu lors de la participation
     * @param resultLabel libellé du résultat obtenu
     * @param playedAt date et heure de la participation
     */
    public WheelParticipation(
            UUID id,
            Wheel wheel,
            Customer customer,
            Ticket ticket,
            WheelPrize wheelPrize,
            String resultLabel,
            LocalDateTime playedAt
    ) {
        this.id = id;
        this.wheel = wheel;
        this.customer = customer;
        this.ticket = ticket;
        this.wheelPrize = wheelPrize;
        this.resultLabel = resultLabel;
        this.playedAt = playedAt;
    }

    public UUID getId() {
        return id;
    }

    public Wheel getWheel() {
        return wheel;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Ticket getTicket() {
        return ticket;
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
