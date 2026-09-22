package com.fidely.backend.api.dtos.mappers.transactions;

import com.fidely.backend.api.dtos.models.transactions.CreateTransactionRequest;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper permettant de transformer une requête de création
 * de transaction en objet métier {@link Ticket}.
 *
 * <p>Ce mapper ne contient aucune logique métier et ne réalise
 * aucun accès à la persistance.</p>
 *
 * <p>L'identifiant du ticket est généré côté serveur et son statut
 * initial est {@link TicketStatus#PENDING}. L'empreinte du ticket
 * est calculée automatiquement par le domaine.</p>
 */
@Component
public class CreateTransactionRequestMapper {

    /**
     * Transforme une requête API en ticket métier temporaire.
     *
     * @param request requête de création de transaction
     * @return ticket métier temporaire en attente de traitement
     * @throws IllegalArgumentException si la requête est null
     */
    public Ticket toTicket(CreateTransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request cannot be null"
            );
        }

        return new Ticket(
                UUID.randomUUID(),
                request.merchantId(),
                request.customerId(),
                request.ticketNumber(),
                request.ticketDate(),
                request.ticketTime(),
                request.amount(),
                request.rawOcrText(),
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }
}