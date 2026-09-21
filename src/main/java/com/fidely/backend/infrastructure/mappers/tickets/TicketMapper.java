package com.fidely.backend.infrastructure.mappers.tickets;

import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.infrastructure.entities.tickets.TicketEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine Ticket et son équivalent JPA.
 */
@Component
public class TicketMapper {

    /**
     * Convertit une entité JPA en modèle de domaine.
     *
     * @param entity entité JPA à convertir
     * @return modèle de domaine correspondant
     */
    public Ticket toDomain(TicketEntity entity) {
        return new Ticket(
                entity.getId(),
                entity.getMerchantId(),
                entity.getCustomerId(),
                entity.getTicketNumber(),
                entity.getFingerprintHash(),
                entity.getTicketDate(),
                entity.getTicketTime(),
                entity.getAmount(),
                entity.getRawOcrText(),
                entity.getStatus(),
                entity.getRejectionReason(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit un modèle de domaine en entité JPA.
     *
     * @param ticket modèle de domaine à convertir
     * @return entité JPA correspondante
     */
    public TicketEntity toEntity(Ticket ticket) {
        return new TicketEntity(
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