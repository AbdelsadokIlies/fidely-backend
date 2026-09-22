package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.loyalties.CreateLoyaltyRequest;
import com.fidely.backend.api.dtos.loyalties.LoyaltyResponse;
import com.fidely.backend.api.dtos.loyalties.LoyaltyTransactionResponse;
import com.fidely.backend.api.dtos.mappers.LoyaltyDtoMapper;
import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST permettant de gérer les programmes de fidélité.
 */
@RestController
@RequestMapping("/api/loyalties")
@Tag(
        name = "Loyalties",
        description = "Gestion des programmes de fidélité"
)
public class LoyaltyController {

    private final ILoyaltyService loyaltyService;
    private final LoyaltyDtoMapper loyaltyDtoMapper;

    /**
     * Crée un contrôleur de gestion des programmes de fidélité.
     *
     * @param loyaltyService service de gestion des programmes de fidélité
     * @param loyaltyDtoMapper mapper entre les modèles et les DTO
     */
    public LoyaltyController(
            ILoyaltyService loyaltyService,
            LoyaltyDtoMapper loyaltyDtoMapper
    ) {
        this.loyaltyService = loyaltyService;
        this.loyaltyDtoMapper = loyaltyDtoMapper;
    }

    /**
     * Crée un nouveau programme de fidélité.
     *
     * @param request données nécessaires à la création
     * @return programme de fidélité créé
     */
    @PostMapping
    @Operation(
            summary = "Créer une fidélité",
            description = "Crée un programme de fidélité pour un client chez un marchand."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Programme de fidélité créé"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide"
            )
    })
    public ResponseEntity<LoyaltyResponse> createLoyalty(
            @Valid @RequestBody CreateLoyaltyRequest request
    ) {
        Loyalty loyalty = loyaltyService.createLoyalty(
                request.customerId(),
                request.merchantId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loyaltyDtoMapper.toResponse(loyalty));
    }

    /**
     * Récupère un programme de fidélité par son identifiant.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @return programme de fidélité trouvé ou réponse 404
     */
    @GetMapping("/{loyaltyId}")
    @Operation(
            summary = "Récupérer une fidélité",
            description = "Récupère un programme de fidélité par son identifiant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Programme de fidélité trouvé"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Programme de fidélité introuvable"
            )
    })
    public ResponseEntity<LoyaltyResponse> getLoyalty(
            @Parameter(
                    description = "Identifiant du programme de fidélité"
            )
            @PathVariable UUID loyaltyId
    ) {
        return loyaltyService.getLoyaltyById(loyaltyId)
                .map(loyaltyDtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    /**
     * Récupère les programmes de fidélité d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des programmes de fidélité
     */
    @GetMapping("/customer/{customerId}")
    @Operation(
            summary = "Récupérer les fidélités d'un client",
            description = "Récupère tous les programmes de fidélité associés à un client."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fidélités récupérées"
    )
    public ResponseEntity<List<LoyaltyResponse>> getLoyaltiesByCustomer(
            @Parameter(description = "Identifiant du client")
            @PathVariable UUID customerId
    ) {
        List<LoyaltyResponse> loyalties = loyaltyService
                .getLoyaltiesByCustomer(customerId)
                .stream()
                .map(loyaltyDtoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(loyalties);
    }

    /**
     * Récupère les programmes de fidélité d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des programmes de fidélité
     */
    @GetMapping("/merchant/{merchantId}")
    @Operation(
            summary = "Récupérer les fidélités d'un marchand",
            description = "Récupère tous les programmes de fidélité associés à un marchand."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fidélités récupérées"
    )
    public ResponseEntity<List<LoyaltyResponse>> getLoyaltiesByMerchant(
            @Parameter(description = "Identifiant du marchand")
            @PathVariable UUID merchantId
    ) {
        List<LoyaltyResponse> loyalties = loyaltyService
                .getLoyaltiesByMerchant(merchantId)
                .stream()
                .map(loyaltyDtoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(loyalties);
    }

    /**
     * Récupère le programme de fidélité d'un client chez un marchand.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return programme de fidélité trouvé ou réponse 404
     */
    @GetMapping("/customer/{customerId}/merchant/{merchantId}")
    @Operation(
            summary = "Récupérer une fidélité client/marchand",
            description = "Récupère le programme de fidélité d'un client chez un marchand."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fidélité trouvée"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fidélité introuvable"
            )
    })
    public ResponseEntity<LoyaltyResponse> getLoyaltyByCustomerAndMerchant(
            @Parameter(description = "Identifiant du client")
            @PathVariable UUID customerId,
            @Parameter(description = "Identifiant du marchand")
            @PathVariable UUID merchantId
    ) {
        return loyaltyService
                .getLoyaltyByCustomerAndMerchant(customerId, merchantId)
                .map(loyaltyDtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Récupère les transactions d'un programme de fidélité.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @return liste des transactions
     */
    @GetMapping("/{loyaltyId}/transactions")
    @Operation(
            summary = "Récupérer les transactions d'une fidélité",
            description = "Récupère l'historique des transactions de points d'une fidélité."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transactions récupérées"
    )
    public ResponseEntity<List<LoyaltyTransactionResponse>> getTransactionsByLoyalty(
            @Parameter(description = "Identifiant du programme de fidélité")
            @PathVariable UUID loyaltyId
    ) {
        List<LoyaltyTransactionResponse> transactions =
                loyaltyService.getTransactionsByLoyalty(loyaltyId)
                        .stream()
                        .map(loyaltyDtoMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(transactions);
    }

    /**
     * Récupère la transaction de fidélité associée à un ticket.
     *
     * @param ticketId identifiant du ticket
     * @return transaction trouvée ou réponse 404
     */
    @GetMapping("/transactions/ticket/{ticketId}")
    @Operation(
            summary = "Récupérer la transaction d'un ticket",
            description = "Récupère la transaction de fidélité associée à un ticket."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction trouvée"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction introuvable"
            )
    })
    public ResponseEntity<LoyaltyTransactionResponse> getTransactionByTicket(
            @Parameter(description = "Identifiant du ticket")
            @PathVariable UUID ticketId
    ) {
        return loyaltyService.getTransactionsByTicket(ticketId)
                .stream()
                .findFirst()
                .map(loyaltyDtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Traite un ticket de caisse afin d'attribuer automatiquement
     * les points de fidélité correspondants.
     *
     * <p>Cette opération constitue le point d'entrée principal du
     * parcours de fidélité. Elle déclenche le traitement du ticket,
     * son analyse OCR, le calcul des points selon la règle de fidélité
     * applicable et leur attribution au programme de fidélité.</p>
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param image image du ticket de caisse
     * @return le programme de fidélité mis à jour
     * @throws IOException si la lecture de l'image échoue
     */
    @PostMapping(
            value = "/{loyaltyId}/earn",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Gagner des points avec un ticket",
            description = "Analyse un ticket de caisse, calcule les points "
                    + "de fidélité et les attribue automatiquement."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Points attribués avec succès"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ticket invalide"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fidélité introuvable"
            )
    })
    public ResponseEntity<LoyaltyResponse> earnPointsFromTicket(
            @Parameter(
                    description = "Identifiant du programme de fidélité"
            )
            @PathVariable UUID loyaltyId,
            @Parameter(
                    description = "Image du ticket de caisse"
            )
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        Loyalty loyalty = loyaltyService.addPointsFromTicket(
                loyaltyId,
                image.getBytes()
        );

        return ResponseEntity.ok(
                loyaltyDtoMapper.toResponse(loyalty)
        );
    }
}