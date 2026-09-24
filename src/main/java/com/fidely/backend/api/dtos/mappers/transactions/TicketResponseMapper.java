package com.fidely.backend.api.dtos.mappers.tickets;

import com.fidely.backend.api.dtos.models.tickets.TicketResponse;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de transformer un objet métier {@link Ticket}
 * en réponse API {@link TicketResponse}.
 *
 * <p>Ce mapper ne contient aucune logique métier et ne réalise
 * aucun accès à la persistance.</p>
 */
@Component
public class TicketResponseMapper {

    /**
     * Transforme un ticket métier en réponse API.
     *
     * @param ticket ticket métier à transformer
     * @return réponse API correspondante
     * @throws IllegalArgumentException si le ticket est null
     */
    public TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket cannot be null"
            );
        }

        return new TicketResponse(
                ticket.getId(),
                ticket.getMerchantId(),
                ticket.getCustomerId(),
                ticket.getTicketNumber(),
                ticket.getFingerprintHash(),
                ticket.getTicketDate(),
                ticket.getTicketTime(),
                ticket.getAmount(),
                ticket.getRawOcrText(),
                ticket.getStatus(),
                ticket.getRejectionReason(),
                ticket.getCreatedAt()
        );
    }
}