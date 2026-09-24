package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketRequest;
import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionRequestMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.ManualTransactionRequestMapper;
import com.fidely.backend.api.dtos.mappers.transactions.TicketResponseMapper;
import com.fidely.backend.api.dtos.models.tickets.OcrTicketResponse;
import com.fidely.backend.api.dtos.models.tickets.TicketResponse;
import com.fidely.backend.api.dtos.models.transactions.CreateTransactionRequest;
import com.fidely.backend.api.dtos.models.transactions.CreateTransactionResponse;
import com.fidely.backend.api.dtos.models.transactions.ManualTransactionRequest;
import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.tickets.Ticket;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller REST responsable de la gestion des transactions liées
 * aux tickets de caisse et des transactions manuelles.
 *
 * <p>Ce controller expose les endpoints permettant notamment
 * d'extraire les données d'un ticket par OCR, de créer une transaction
 * de fidélité à partir des données extraites, de récupérer un ticket
 * persisté et d'enregistrer directement une transaction manuelle.</p>
 *
 * <p>La logique métier est déléguée aux ports d'entrée applicatifs.
 * Le controller se limite à gérer les requêtes HTTP, appeler les
 * services applicatifs et transformer les résultats en réponses API.</p>
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final ITicketService ticketService;
    private final ILoyaltyService loyaltyService;
    private final OcrTicketResponseMapper ocrTicketResponseMapper;
    private final TicketResponseMapper ticketResponseMapper;
    private final CreateTransactionRequestMapper createTransactionRequestMapper;
    private final CreateTransactionResponseMapper createTransactionResponseMapper;
    private final ManualTransactionRequestMapper manualTransactionRequestMapper;

    /**
     * Construit le controller des transactions.
     *
     * @param ticketService service applicatif de gestion des tickets
     * @param loyaltyService service applicatif de gestion des fidélités
     * @param ocrTicketResponseMapper mapper des réponses OCR
     * @param ticketResponseMapper mapper des réponses de ticket
     * @param createTransactionRequestMapper mapper des requêtes
     *                                   de création de transaction
     * @param createTransactionResponseMapper mapper des réponses
     *                                    de création de transaction
     * @param manualTransactionRequestMapper mapper des requêtes
     *                                      de transaction manuelle
     */
    public TransactionController(
            ITicketService ticketService,
            ILoyaltyService loyaltyService,
            OcrTicketResponseMapper ocrTicketResponseMapper,
            TicketResponseMapper ticketResponseMapper,
            CreateTransactionRequestMapper createTransactionRequestMapper,
            CreateTransactionResponseMapper createTransactionResponseMapper,
            ManualTransactionRequestMapper manualTransactionRequestMapper
    ) {
        this.ticketService = ticketService;
        this.loyaltyService = loyaltyService;
        this.ocrTicketResponseMapper = ocrTicketResponseMapper;
        this.ticketResponseMapper = ticketResponseMapper;
        this.createTransactionRequestMapper = createTransactionRequestMapper;
        this.createTransactionResponseMapper = createTransactionResponseMapper;
        this.manualTransactionRequestMapper = manualTransactionRequestMapper;
    }

    /**
     * Extrait les données d'un ticket de caisse à partir d'une image.
     *
     * <p>Le ticket créé par cette opération est temporaire et n'est pas
     * persisté en base de données. Il pourra ensuite être utilisé dans
     * le workflow de création de transaction.</p>
     *
     * @param request requête multipart contenant les identifiants
     *                du marchand et du client ainsi que l'image du ticket
     * @return réponse contenant les données extraites du ticket
     */
    @PostMapping(
            path = "/ocr",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OcrTicketResponse> extractTicketFromOcr(
            @Valid @ModelAttribute OcrTicketRequest request
    ) {
        final byte[] image;

        try {
            image = request.image().getBytes();
        } catch (java.io.IOException exception) {
            throw new IllegalArgumentException(
                    "Impossible de lire l'image du ticket.",
                    exception
            );
        }

        Ticket ticket = ticketService.extractTicketFromOcr(
                image,
                request.merchantId(),
                request.customerId()
        );

        OcrTicketResponse response =
                ocrTicketResponseMapper.toResponse(ticket);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère un ticket de caisse persisté à partir de son identifiant.
     *
     * <p>Si aucun ticket ne correspond à l'identifiant fourni,
     * l'API retourne une réponse HTTP {@code 404 Not Found}.</p>
     *
     * @param ticketId identifiant du ticket à récupérer
     * @return ticket correspondant ou réponse HTTP {@code 404}
     */
    @GetMapping(
            path = "/{ticketId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TicketResponse> getTransaction(
            @PathVariable UUID ticketId
    ) {
        return ticketService.getTicketById(ticketId)
                .map(ticketResponseMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crée une transaction de fidélité à partir des données
     * d'un ticket préalablement extrait par OCR.
     *
     * <p>Le ticket est reconstruit sous forme d'objet métier puis
     * transmis au service de fidélité. Celui-ci vérifie le ticket,
     * calcule les points, met à jour le solde de fidélité et persiste
     * les éléments nécessaires à la transaction.</p>
     *
     * @param request requête contenant l'identifiant de fidélité
     *                et les données du ticket
     * @return fidélité mise à jour après attribution des points
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CreateTransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        Ticket ticket = createTransactionRequestMapper.toTicket(request);

        Loyalty loyalty = loyaltyService.addPointsFromTicket(
                request.loyaltyId(),
                ticket
        );

        CreateTransactionResponse response =
                createTransactionResponseMapper.toResponse(loyalty);

        return ResponseEntity.ok(response);
    }

    /**
     * Enregistre manuellement une transaction de fidélité.
     *
     * <p>Cette opération est utilisée lorsqu'un marchand souhaite
     * attribuer directement des points à un client sans passer
     * par l'extraction d'un ticket de caisse.</p>
     *
     * <p>Le service applicatif détermine la règle de points applicable,
     * calcule les points à attribuer, met à jour le solde de fidélité
     * et enregistre la transaction.</p>
     *
     * @param request requête contenant l'identifiant de fidélité,
     *                les identifiants du marchand et du client ainsi
     *                que le montant de la transaction
     * @return fidélité mise à jour après attribution des points
     */
    @PostMapping(
            path = "/manual",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CreateTransactionResponse> createManualTransaction(
            @Valid @RequestBody ManualTransactionRequest request
    ) {
        ManualTransactionRequest validatedRequest =
                manualTransactionRequestMapper.toRequest(request);

        Loyalty loyalty = loyaltyService.addPointsManually(
                validatedRequest.loyaltyId(),
                validatedRequest.merchantId(),
                validatedRequest.customerId(),
                validatedRequest.amount()
        );

        CreateTransactionResponse response =
                createTransactionResponseMapper.toResponse(loyalty);

        return ResponseEntity.ok(response);
    }
}