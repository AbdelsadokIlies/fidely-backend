package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.loyalties.CreateLoyaltyRequest;
import com.fidely.backend.api.dtos.loyalties.LoyaltyResponse;
import com.fidely.backend.api.dtos.loyalties.LoyaltyTransactionResponse;
import com.fidely.backend.api.dtos.loyalties.rewards.PointsRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Ajoute des points à un programme de fidélité.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param request nombre de points à ajouter
     * @return réponse sans contenu
     */
    @PostMapping("/{loyaltyId}/points")
    @Operation(
            summary = "Ajouter des points",
            description = "Ajoute des points au solde d'une fidélité."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Points ajoutés"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fidélité introuvable"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nombre de points invalide"
            )
    })
    public ResponseEntity<Void> addPoints(
            @PathVariable UUID loyaltyId,
            @Valid @RequestBody PointsRequest request
    ) {
        loyaltyService.addPoints(
                loyaltyId,
                request.points()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Retire des points d'un programme de fidélité.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param request nombre de points à retirer
     * @return réponse sans contenu
     */
    @PostMapping("/{loyaltyId}/points/deduct")
    @Operation(
            summary = "Retirer des points",
            description = "Retire des points du solde d'une fidélité."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Points retirés"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nombre de points invalide ou solde insuffisant"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fidélité introuvable"
            )
    })
    public ResponseEntity<Void> removePoints(
            @PathVariable UUID loyaltyId,
            @Valid @RequestBody PointsRequest request
    ) {
        loyaltyService.removePoints(
                loyaltyId,
                request.points()
        );

        return ResponseEntity.noContent().build();
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
}