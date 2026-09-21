package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.tickets.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Définit les opérations applicatives liées aux tickets.
 */
public interface ITicketService {

    /**
     * Crée un nouveau ticket.
     *
     * @param ticket ticket à créer
     * @return le ticket créé
     */
    Ticket createTicket(Ticket ticket);

    /**
     * Crée un ticket à partir des informations extraites par OCR.
     *
     * @param ticket ticket construit à partir des données OCR
     * @return le ticket créé
     */
    Ticket createTicketFromOcr(Ticket ticket);

    /**
     * Recherche un ticket à partir de son identifiant.
     *
     * @param ticketId identifiant du ticket
     * @return le ticket s'il existe
     */
    Optional<Ticket> getTicketById(UUID ticketId);

    /**
     * Recherche un ticket à partir de son numéro.
     *
     * @param ticketNumber numéro du ticket
     * @return le ticket s'il existe
     */
    Optional<Ticket> getTicketByNumber(String ticketNumber);

    /**
     * Récupère les tickets associés à un client.
     *
     * @param customerId identifiant du client
     * @return liste des tickets du client
     */
    List<Ticket> getTicketsByCustomer(UUID customerId);

    /**
     * Récupère les tickets associés à un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des tickets du marchand
     */
    List<Ticket> getTicketsByMerchant(UUID merchantId);

    /**
     * Récupère les tickets d'un client chez un marchand donné.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return liste des tickets correspondants
     */
    List<Ticket> getTicketsByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    );

    /**
     * Valide un ticket.
     *
     * @param ticketId identifiant du ticket à valider
     */
    void validateTicket(UUID ticketId);

    /**
     * Rejette un ticket.
     *
     * @param ticketId identifiant du ticket à rejeter
     * @param reason raison du rejet
     */
    void rejectTicket(UUID ticketId, String reason);

    /**
     * Marque un ticket comme doublon.
     *
     * @param ticketId identifiant du ticket à marquer comme doublon
     */
    void markAsDuplicate(UUID ticketId);
}
