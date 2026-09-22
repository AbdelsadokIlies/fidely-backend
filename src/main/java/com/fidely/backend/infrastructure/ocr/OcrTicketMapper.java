package com.fidely.backend.infrastructure.ocr;

import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper permettant de convertir les données OCR en objet métier Ticket.
 */
@Component
public class OcrTicketMapper {

    /**
     * Convertit les données extraites par OCR en ticket métier.
     *
     * @param data données extraites du ticket
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @param fingerprintHash empreinte permettant d'identifier le ticket
     * @return le ticket métier correspondant aux données OCR
     */
    public Ticket toDomain(
            OcrTicketData data,
            UUID merchantId,
            UUID customerId,
            String fingerprintHash
    ) {
        return new Ticket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                data.ticketNumber(),
                fingerprintHash,
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