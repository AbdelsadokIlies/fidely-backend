package com.fidely.backend.api.dtos.models.tickets;

import com.fidely.backend.domain.models.tickets.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Réponse API représentant un ticket de caisse persisté.
 *
 * <p>Cette réponse expose les informations du ticket nécessaires
 * à sa consultation par le client ou le marchand.</p>
 *
 * @param id identifiant du ticket
 * @param merchantId identifiant du marchand associé au ticket
 * @param customerId identifiant du client associé au ticket
 * @param ticketNumber numéro du ticket
 * @param fingerprintHash empreinte permettant d'identifier le ticket
 * @param ticketDate date du ticket
 * @param ticketTime heure du ticket
 * @param amount montant du ticket
 * @param rawOcrText texte brut extrait par OCR
 * @param status état actuel du ticket
 * @param rejectionReason raison du rejet si le ticket est rejeté
 * @param createdAt date et heure de création du ticket
 */
public record TicketResponse(
        UUID id,
        UUID merchantId,
        UUID customerId,
        String ticketNumber,
        String fingerprintHash,
        LocalDate ticketDate,
        LocalTime ticketTime,
        BigDecimal amount,
        String rawOcrText,
        TicketStatus status,
        String rejectionReason,
        LocalDateTime createdAt
) { }