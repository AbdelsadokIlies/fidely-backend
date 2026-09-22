package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.tickets.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrée pour la gestion des tickets.
 *
 * <p>Ce service permet de créer, rechercher et gérer les tickets associés
 * aux clients et aux marchands.</p>
 */
public interface ITicketService {

    /**
     * Crée et persiste un ticket.
     *
     * @param ticket ticket à créer
     * @return ticket créé
     */
    Ticket createTicket(Ticket ticket);

    /**
     * Extrait les données d'un ticket à partir d'une image OCR.
     *
     * <p>Cette opération effectue uniquement l'extraction OCR et la
     * transformation des données extraites en objet métier {@link Ticket}.
     * Le ticket retourné n'est pas persisté.</p>
     *
     * @param image image du ticket à analyser
     * @param merchantId identifiant du marchand associé au ticket
     * @param customerId identifiant du client associé au ticket
     * @return ticket métier temporaire en attente de validation
     */
    Ticket extractTicketFromOcr(
            byte[] image,
            UUID merchantId,
            UUID customerId
    );

    /**
     * Recherche un ticket à partir de son identifiant.
     *
     * @param ticketId identifiant du ticket
     * @return ticket correspondant s'il existe
     */
    Optional<Ticket> getTicketById(UUID ticketId);

    /**
     * Recherche un ticket à partir de son numéro.
     *
     * @param ticketNumber numéro du ticket
     * @return ticket correspondant s'il existe
     */
    Optional<Ticket> getTicketByNumber(String ticketNumber);

    /**
     * Récupère tous les tickets d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des tickets du client
     */
    List<Ticket> getTicketsByCustomer(UUID customerId);

    /**
     * Récupère tous les tickets d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des tickets du marchand
     */
    List<Ticket> getTicketsByMerchant(UUID merchantId);

    /**
     * Récupère les tickets d'un client pour un marchand donné.
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
     * @param ticketId identifiant du ticket
     */
    void validateTicket(UUID ticketId);

    /**
     * Rejette un ticket avec une raison.
     *
     * @param ticketId identifiant du ticket
     * @param reason raison du rejet
     */
    void rejectTicket(UUID ticketId, String reason);

    /**
     * Marque un ticket comme doublon.
     *
     * @param ticketId identifiant du ticket
     */
    void markAsDuplicate(UUID ticketId);

    /**
     * Vérifie si un ticket possédant l'empreinte fournie existe déjà.
     *
     * @param fingerprintHash empreinte du ticket
     * @return {@code true} si un ticket correspondant existe déjà,
     *         {@code false} sinon
     */
    boolean existsByFingerprintHash(String fingerprintHash);
}