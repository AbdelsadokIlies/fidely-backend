package com.fidely.backend.infrastructure.ocr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Contient les données extraites d'un ticket par le service OCR.
 *
 * @param ticketNumber numéro du ticket
 * @param ticketDate date du ticket
 * @param ticketTime heure du ticket
 * @param amount montant total du ticket
 * @param rawText texte brut extrait du ticket
 */
public record OcrTicketData(
        String ticketNumber,
        LocalDate ticketDate,
        LocalTime ticketTime,
        BigDecimal amount,
        String rawText
) {
}