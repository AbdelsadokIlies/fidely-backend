package com.fidely.backend.application;

import com.fidely.backend.application.port.out.ITicketRepository;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation des opérations applicatives liées aux tickets.
 *
 * <p>Cette classe orchestre les opérations entre les ports applicatifs
 * et le domaine sans dépendre directement de l'implémentation de la
 * persistance.</p>
 */
@Service
public class TicketService implements ITicketService {

    private final ITicketRepository ticketRepository;

    /**
     * Crée un service de gestion des tickets.
     *
     * @param ticketRepository port de persistance des tickets
     */
    public TicketService(ITicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ticket createTicketFromOcr(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Ticket> getTicketById(UUID ticketId) {
        return ticketRepository.findById(ticketId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Ticket> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Ticket> getTicketsByCustomer(UUID customerId) {
        return ticketRepository.findByCustomerId(customerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Ticket> getTicketsByMerchant(UUID merchantId) {
        return ticketRepository.findByMerchantId(merchantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Ticket> getTicketsByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    ) {
        return ticketRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateTicket(UUID ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        ticket.validate();

        ticketRepository.save(ticket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectTicket(UUID ticketId, String reason) {
        Ticket ticket = getTicketOrThrow(ticketId);

        ticket.reject(reason);

        ticketRepository.save(ticket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsDuplicate(UUID ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        ticket.markAsDuplicate();

        ticketRepository.save(ticket);
    }

    /**
     * Récupère un ticket ou lève une exception s'il n'existe pas.
     *
     * @param ticketId identifiant du ticket
     * @return ticket correspondant à l'identifiant
     * @throws IllegalArgumentException si le ticket n'existe pas
     */
    private Ticket getTicketOrThrow(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket introuvable : " + ticketId
                        )
                );
    }
}