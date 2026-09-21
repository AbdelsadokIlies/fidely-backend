package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.tickets.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Définit les opérations applicatives liées à la gestion des tickets.
 *
 * <p>Ce port d'entrée permet aux contrôleurs et autres composants
 * applicatifs d'interagir avec la gestion des tickets sans dépendre
 * directement de l'implémentation du service.</p>
 */
public interface ITicketService {

    /**
     * Crée un nouveau ticket à partir d'un ticket déjà construit.
     *
     * @param ticket ticket à créer
     * @return le ticket créé et sauvegardé
     */
    Ticket createTicket(Ticket ticket);

    /**
     * Crée un ticket à partir d'une image de ticket de caisse.
     *
     * <p>L'image est transmise au service OCR afin d'en extraire
     * les informations nécessaires à la création du ticket.</p>
     *
     * @param image image du ticket de caisse à analyser
     * @param merchantId identifiant du marchand associé au ticket
     * @param customerId identifiant du client associé au ticke
     * @return le ticket créé et sauvegardé
     */
    Ticket createTicketFromOcr(
            byte[] image,
            UUID merchantId,
            UUID customerId
    );

    /**
     * Recherche un ticket à partir de son identifiant.
     *
     * @param ticketId identifiant du ticket recherché
     * @return le ticket s'il existe, sinon un Optional vide
     */
    Optional<Ticket> getTicketById(UUID ticketId);

    /**
     * Recherche un ticket à partir de son numéro.
     *
     * @param ticketNumber numéro du ticket recherché
     * @return le ticket s'il existe, sinon un Optional vide
     */
    Optional<Ticket> getTicketByNumber(String ticketNumber);

    /**
     * Récupère l'ensemble des tickets associés à un client.
     *
     * @param customerId identifiant du client
     * @return liste des tickets associés au client
     */
    List<Ticket> getTicketsByCustomer(UUID customerId);

    /**
     * Récupère l'ensemble des tickets associés à un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des tickets associés au marchand
     */
    List<Ticket> getTicketsByMerchant(UUID merchantId);

    /**
     * Récupère les tickets d'un client chez un marchand donné.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return liste des tickets correspondant au client et au marchand
     */
    List<Ticket> getTicketsByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    );

    /**
     * Valide un ticket.
     *
     * <p>La validation est effectuée par le domaine du ticket.
     * Seul un ticket actuellement en attente peut être validé.</p>
     *
     * @param ticketId identifiant du ticket à valider
     * @throws IllegalArgumentException si le ticket n'existe pas
     * @throws IllegalStateException si le ticket ne peut pas être validé
     */
    void validateTicket(UUID ticketId);

    /**
     * Rejette un ticket avec une raison.
     *
     * <p>La raison du rejet est conservée avec le ticket afin
     * de permettre d'expliquer pourquoi celui-ci a été rejeté.</p>
     *
     * @param ticketId identifiant du ticket à rejeter
     * @param reason raison du rejet
     * @throws IllegalArgumentException si le ticket n'existe pas
     * @throws IllegalStateException si le ticket ne peut pas être rejeté
     */
    void rejectTicket(UUID ticketId, String reason);

    /**
     * Marque un ticket comme doublon.
     *
     * <p>Cette opération permet d'indiquer qu'un ticket a déjà été
     * utilisé ou qu'il correspond à un ticket existant.</p>
     *
     * @param ticketId identifiant du ticket à marquer comme doublon
     * @throws IllegalArgumentException si le ticket n'existe pas
     * @throws IllegalStateException si le ticket ne peut pas être marqué
     *                              comme doublon
     */
    void markAsDuplicate(UUID ticketId);
}