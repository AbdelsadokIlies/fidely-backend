package com.fidely.backend.api.dtos.models.tickets;

import com.fidely.backend.domain.models.tickets.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Réponse retournée après l'extraction OCR d'un ticket de caisse.
 *
 * <p>Cette réponse représente un ticket métier temporaire qui n'est pas
 * encore persisté en base de données. Le client peut utiliser les données
 * extraites afin de permettre la validation du ticket avant sa création
 * effective de transaction.</p>
 *
 * @param id identifiant temporaire du ticket
 * @param merchantId identifiant du marchand associé au ticket
 * @param customerId identifiant du client associé au ticket
 * @param ticketNumber numéro du ticket extrait
 * @param ticketDate date du ticket extraite
 * @param ticketTime heure du ticket extraite
 * @param amount montant du ticket extrait
 * @param rawOcrText texte brut retourné par l'OCR
 * @param status statut actuel du ticket temporaire
 */
public record OcrTicketResponse(
        UUID id,
        UUID merchantId,
        UUID customerId,
        String ticketNumber,
        LocalDate ticketDate,
        LocalTime ticketTime,
        BigDecimal amount,
        String rawOcrText,
        TicketStatus status
) {
}