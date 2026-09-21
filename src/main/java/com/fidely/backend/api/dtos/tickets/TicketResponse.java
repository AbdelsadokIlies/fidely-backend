package com.fidely.backend.api.dtos.tickets;

import com.fidely.backend.domain.models.tickets.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Représente les informations d'un ticket exposées par l'API.
 *
 * <p>Ce DTO permet de découpler la représentation HTTP du modèle
 * de domaine {@link com.fidely.backend.domain.models.tickets.Ticket}.</p>
 *
 * @param id identifiant du ticket
 * @param merchantId identifiant du marchand associé au ticket
 * @param customerId identifiant du client associé au ticket
 * @param ticketNumber numéro du ticket
 * @param ticketDate date du ticket
 * @param ticketTime heure du ticket
 * @param amount montant du ticket
 * @param status statut actuel du ticket
 * @param rejectionReason raison du rejet si le ticket est rejeté
 * @param createdAt date de création du ticket
 */
public record TicketResponse(
        UUID id,
        UUID merchantId,
        UUID customerId,
        String ticketNumber,
        LocalDate ticketDate,
        LocalTime ticketTime,
        BigDecimal amount,
        TicketStatus status,
        String rejectionReason,
        LocalDateTime createdAt
) {
}