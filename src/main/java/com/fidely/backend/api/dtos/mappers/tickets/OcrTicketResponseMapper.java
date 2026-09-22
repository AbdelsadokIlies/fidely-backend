package com.fidely.backend.api.dtos.mappers.tickets;

import com.fidely.backend.api.dtos.models.tickets.OcrTicketResponse;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de transformer un ticket métier en réponse API
 * utilisée lors de l'extraction OCR.
 *
 * <p>Ce mapper ne contient aucune logique métier et ne réalise aucun
 * accès à la persistance.</p>
 */
@Component
public class OcrTicketResponseMapper {

    /**
     * Convertit un ticket métier en réponse API.
     *
     * @param ticket ticket métier à convertir
     * @return réponse API correspondante
     * @throws IllegalArgumentException si le ticket est null
     */
    public OcrTicketResponse toResponse(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket cannot be null"
            );
        }

        return new OcrTicketResponse(
                ticket.getId(),
                ticket.getMerchantId(),
                ticket.getCustomerId(),
                ticket.getTicketNumber(),
                ticket.getTicketDate(),
                ticket.getTicketTime(),
                ticket.getAmount(),
                ticket.getRawOcrText(),
                ticket.getStatus()
        );
    }
}