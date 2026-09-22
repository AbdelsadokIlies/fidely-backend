package com.fidely.backend.infrastructure.ocr;

import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper permettant de transformer les données extraites par OCR
 * en objet métier {@link Ticket}.
 *
 * <p>Le ticket créé par ce mapper est un objet métier temporaire.
 * Il n'est pas persisté à ce stade du workflow.</p>
 */
@Component
public class OcrTicketMapper {

    /**
     * Transforme les données extraites par OCR en ticket métier.
     *
     * <p>L'empreinte du ticket est calculée automatiquement par le
     * modèle de domaine {@link Ticket} à partir de ses données métier.</p>
     *
     * @param data données extraites par le service OCR
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @return ticket métier temporaire en attente de validation
     * @throws IllegalArgumentException si les données OCR sont nulles
     */
    public Ticket toDomain(
            OcrTicketData data,
            UUID merchantId,
            UUID customerId
    ) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "OCR data cannot be null"
            );
        }

        return new Ticket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                data.ticketNumber(),
                data.ticketDate(),
                data.ticketTime(),
                data.amount(),
                data.rawText(),
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }
}