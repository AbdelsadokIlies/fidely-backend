package com.fidely.backend.application;

import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.IOcrService;
import com.fidely.backend.application.port.out.ITicketRepository;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.infrastructure.ocr.OcrTicketData;
import com.fidely.backend.infrastructure.ocr.OcrTicketMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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
 * <p>Lorsqu'un ticket est créé à partir d'une image, une empreinte
 * déterministe SHA-256 est calculée à partir des informations extraites
 * du ticket. Cette empreinte permet de détecter la soumission multiple
 * d'un même ticket.</p>
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
     * Analyse une image de ticket et crée le ticket correspondant.
     *
     * <p>Les données sont d'abord extraites par le service OCR.
     * Une empreinte déterministe est ensuite calculée à partir
     * de l'identifiant du marchand et des données du ticket.</p>
     *
     * <p>Avant de créer le ticket, le service vérifie si cette empreinte
     * existe déjà. Si c'est le cas, l'opération est refusée afin
     * d'empêcher l'utilisation multiple d'un même ticket.</p>
     *
     * @param image image du ticket de caisse
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @return ticket créé et persisté
     * @throws IllegalStateException si le ticket a déjà été utilisé
     */
    @Override
    public Ticket createTicketFromOcr(
            byte[] image,
            UUID merchantId,
            UUID customerId
    ) {
        OcrTicketData ocrData =
                ocrService.extractTicketData(image);

        String fingerprintHash =
                generateFingerprintHash(
                        merchantId,
                        ocrData
                );

        if (ticketRepository.existsByFingerprintHash(fingerprintHash)) {
            throw new IllegalStateException(
                    "Ce ticket a déjà été utilisé."
            );
        }

        Ticket ticket = ocrTicketMapper.toDomain(
                ocrData,
                merchantId,
                customerId,
                fingerprintHash
        );

        return ticketRepository.save(ticket);
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
     * Génère une empreinte SHA-256 déterministe pour un ticket.
     *
     * <p>L'empreinte est calculée à partir des informations suivantes :</p>
     *
     * <ul>
     *     <li>identifiant du marchand ;</li>
     *     <li>numéro du ticket ;</li>
     *     <li>date du ticket ;</li>
     *     <li>heure du ticket ;</li>
     *     <li>montant du ticket.</li>
     * </ul>
     *
     * <p>Deux tickets produisant exactement les mêmes données
     * produiront donc la même empreinte.</p>
     *
     * @param merchantId identifiant du marchand
     * @param data données extraites par l'OCR
     * @return empreinte SHA-256 encodée en hexadécimal
     * @throws IllegalStateException si l'algorithme SHA-256
     * n'est pas disponible
     */
    private String generateFingerprintHash(
            UUID merchantId,
            OcrTicketData data
    ) {
        String fingerprintData = String.join(
                "|",
                merchantId.toString(),
                data.ticketNumber(),
                data.ticketDate().toString(),
                data.ticketTime().toString(),
                data.amount().toPlainString()
        );

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            fingerprintData.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 indisponible",
                    exception
            );
        }
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