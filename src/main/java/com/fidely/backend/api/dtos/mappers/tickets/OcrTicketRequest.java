package com.fidely.backend.api.dtos.mappers.tickets;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Requête utilisée pour demander l'extraction des données
 * d'un ticket de caisse par OCR.
 *
 * <p>La requête contient l'identité du marchand et du client
 * ainsi que l'image du ticket à analyser.</p>
 *
 * @param merchantId identifiant du marchand associé au ticket
 * @param customerId identifiant du client associé au ticket
 * @param image image du ticket à transmettre au service OCR
 */
public record OcrTicketRequest(
        @NotNull(message = "Merchant ID is required")
        UUID merchantId,

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Ticket image is required")
        MultipartFile image
) {
}