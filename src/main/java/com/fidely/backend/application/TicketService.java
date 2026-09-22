package com.fidely.backend.application;

import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.IOcrService;
import com.fidely.backend.application.port.out.ITicketRepository;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.infrastructure.ocr.OcrTicketData;
import com.fidely.backend.infrastructure.ocr.OcrTicketMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsable de la gestion des tickets de caisse.
 *
 * <p>Ce service orchestre la création, la récupération et la modification
 * de l'état des tickets. Il prend également en charge la transformation
 * des données issues de l'OCR en ticket métier.</p>
 *
 * <p>L'extraction OCR produit un ticket métier temporaire qui n'est pas
 * persisté. La persistance du ticket intervient uniquement lorsque
 * l'opération de création de transaction est validée.</p>
 */
@Service
public class TicketService implements ITicketService {

    private final ITicketRepository ticketRepository;
    private final IOcrService ocrService;
    private final OcrTicketMapper ocrTicketMapper;

    /**
     * Construit le service de gestion des tickets.
     *
     * @param ticketRepository repository des tickets
     * @param ocrService service d'extraction des données OCR
     * @param ocrTicketMapper mapper des données OCR vers le domaine
     */
    public TicketService(
            ITicketRepository ticketRepository,
            IOcrService ocrService,
            OcrTicketMapper ocrTicketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.ocrService = ocrService;
        this.ocrTicketMapper = ocrTicketMapper;
    }

    /**
     * Crée et persiste un ticket.
     *
     * @param ticket ticket à créer
     * @return ticket persisté
     */
    @Override
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Analyse une image de ticket et transforme les données extraites
     * en ticket métier.
     *
     * <p>Cette méthode ne persiste pas le ticket. Le ticket retourné
     * représente uniquement les données extraites par l'OCR et reste
     * temporaire jusqu'à sa validation lors de la création de la transaction.</p>
     *
     * <p>L'empreinte du ticket est calculée automatiquement par le domaine
     * lors de la création du {@link Ticket}.</p>
     *
     * @param image image du ticket de caisse
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @return ticket métier temporaire en attente de validation
     */
    @Override
    public Ticket extractTicketFromOcr(
            byte[] image,
            UUID merchantId,
            UUID customerId
    ) {
        OcrTicketData ocrData =
                ocrService.extractTicketData(image);

        return ocrTicketMapper.toDomain(
                ocrData,
                merchantId,
                customerId
        );
    }

    /**
     * Récupère un ticket par son identifiant.
     *
     * @param ticketId identifiant du ticket
     * @return ticket trouvé ou {@link Optional#empty()} s'il n'existe pas
     */
    @Override
    public Optional<Ticket> getTicketById(UUID ticketId) {
        return ticketRepository.findById(ticketId);
    }

    /**
     * Récupère un ticket par son numéro.
     *
     * @param ticketNumber numéro du ticket
     * @return ticket trouvé ou {@link Optional#empty()} s'il n'existe pas
     */
    @Override
    public Optional<Ticket> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    /**
     * Récupère tous les tickets d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des tickets du client
     */
    @Override
    public List<Ticket> getTicketsByCustomer(UUID customerId) {
        return ticketRepository.findByCustomerId(customerId);
    }

    /**
     * Récupère tous les tickets d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des tickets du marchand
     */
    @Override
    public List<Ticket> getTicketsByMerchant(UUID merchantId) {
        return ticketRepository.findByMerchantId(merchantId);
    }

    /**
     * Récupère les tickets d'un client chez un marchand.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return liste des tickets correspondants
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
     * Valide un ticket en attente.
     *
     * @param ticketId identifiant du ticket
     * @throws IllegalArgumentException si le ticket n'existe pas
     * @throws IllegalStateException si le ticket ne peut pas être validé
     */
    @Override
    public void validateTicket(UUID ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        ticket.validate();

        ticketRepository.save(ticket);
    }

    /**
     * Rejette un ticket en attente.
     *
     * @param ticketId identifiant du ticket
     * @param reason raison du rejet
     * @throws IllegalArgumentException si le ticket n'existe pas
     * @throws IllegalStateException si le ticket ne peut pas être rejeté
     */
    @Override
    public void rejectTicket(
            UUID ticketId,
            String reason
    ) {
        Ticket ticket = getTicketOrThrow(ticketId);

        ticket.reject(reason);

        ticketRepository.save(ticket);
    }

    /**
     * Marque un ticket en attente comme doublon.
     *
     * @param ticketId identifiant du ticket
     * @throws IllegalArgumentException si le ticket n'existe pas
     * @throws IllegalStateException si le ticket ne peut pas être marqué
     * comme doublon
     */
    @Override
    public void markAsDuplicate(UUID ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);

        ticket.markAsDuplicate();

        ticketRepository.save(ticket);
    }

    /**
     * Vérifie si un ticket possédant l'empreinte fournie existe déjà.
     *
     * @param fingerprintHash empreinte du ticket
     * @return {@code true} si un ticket correspondant existe déjà,
     * {@code false} sinon
     */
    @Override
    public boolean existsByFingerprintHash(String fingerprintHash) {
        return ticketRepository.existsByFingerprintHash(fingerprintHash);
    }

    /**
     * Récupère un ticket ou lève une exception s'il n'existe pas.
     *
     * @param ticketId identifiant du ticket
     * @return ticket trouvé
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