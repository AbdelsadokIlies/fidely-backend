package com.fidely.backend.api.dtos.mappers;

import com.fidely.backend.api.dtos.tickets.TicketResponse;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.springframework.stereotype.Component;

/**
 * Convertit les objets du domaine Ticket en DTOs destinés à l'API REST.
 *
 * <p>Ce mapper permet de maintenir une séparation entre le modèle
 * de domaine et les objets exposés par l'API.</p>
 */
@Component
public class TicketDtoMapper {

    /**
     * Convertit un ticket du domaine en réponse API.
     *
     * @param ticket ticket du domaine à convertir
     * @return DTO représentant le ticket pour l'API
     */
    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getMerchantId(),
                ticket.getCustomerId(),
                ticket.getTicketNumber(),
                ticket.getTicketDate(),
                ticket.getTicketTime(),
                ticket.getAmount(),
                ticket.getStatus(),
                ticket.getRejectionReason(),
                ticket.getCreatedAt()
        );
    }
}