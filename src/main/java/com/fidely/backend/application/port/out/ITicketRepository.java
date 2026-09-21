package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.tickets.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistance permettant au domaine d'accéder aux tickets.
 *
 * <p>Cette interface ne dépend d'aucune technologie de persistance
 * telle que JPA ou Spring Data.</p>
 */
public interface ITicketRepository {

    /**
     * Recherche un ticket à partir de son identifiant.
     *
     * @param id identifiant du ticket
     * @return le ticket s'il existe
     */
    Optional<Ticket> findById(UUID id);

    /**
     * Recherche un ticket à partir de son numéro.
     *
     * @param ticketNumber numéro du ticket
     * @return le ticket s'il existe
     */
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    /**
     * Récupère les tickets associés à un client.
     *
     * @param customerId identifiant du client
     * @return liste des tickets du client
     */
    List<Ticket> findByCustomerId(UUID customerId);

    /**
     * Récupère les tickets associés à un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des tickets du marchand
     */
    List<Ticket> findByMerchantId(UUID merchantId);

    /**
     * Récupère les tickets d'un client chez un marchand donné.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return liste des tickets correspondants
     */
    List<Ticket> findByCustomerIdAndMerchantId(
            UUID customerId,
            UUID merchantId
    );

    /**
     * Sauvegarde un ticket.
     *
     * @param ticket ticket à sauvegarder
     * @return ticket sauvegardé
     */
    Ticket save(Ticket ticket);

    /**
     * Supprime un ticket à partir de son identifiant.
     *
     * @param id identifiant du ticket
     */
    void deleteById(UUID id);

    /**
     * Récupère le ticket selon son empreinte hashée.
     *
     * @param fingerprintHash l'enpreinte hashé du ticket
     * @return un ticket correspondant si trouvé
     */
    Optional<Ticket> findByFingerprintHash(String fingerprintHash);

    /**
     * vérifie l'existancce d'un ticket via son empreinte hashée.
     *
     * @param fingerprintHash l'enpreinte hashé du ticket
     * @return le résultat de la vérification
     */
    boolean existsByFingerprintHash(String fingerprintHash);
}