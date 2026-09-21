package com.fidely.backend.repository;

import com.fidely.backend.repository.entities.tickets.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux tickets en base de données.
 */
public interface SpringDataTicketRepository
        extends JpaRepository<TicketEntity, UUID> {

    /**
     * Recherche un ticket à partir de son numéro.
     *
     * @param ticketNumber numéro du ticket
     * @return le ticket s'il existe
     */
    Optional<TicketEntity> findByTicketNumber(String ticketNumber);

    /**
     * Récupère les tickets associés à un client.
     *
     * @param customerId identifiant du client
     * @return liste des tickets du client
     */
    List<TicketEntity> findByCustomerId(UUID customerId);

    /**
     * Récupère les tickets associés à un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des tickets du marchand
     */
    List<TicketEntity> findByMerchantId(UUID merchantId);

    /**
     * Récupère les tickets d'un client chez un marchand donné.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return liste des tickets correspondants
     */
    List<TicketEntity> findByCustomerIdAndMerchantId(
            UUID customerId,
            UUID merchantId
    );

    /**
     * Récupère le ticket selon son empreinte hashée.
     *
     * @param fingerprintHash l'enpreinte hashé du ticket
     * @return un ticket correspondant si trouvé
     */
    Optional<TicketEntity> findByFingerprintHash(String fingerprintHash);

    /**
     * vérifie l'existancce d'un ticket via son empreinte hashée.
     *
     * @param fingerprintHash l'enpreinte hashé du ticket
     * @return le résultat de la vérification
     */
    boolean existsByFingerprintHash(String fingerprintHash);
}
