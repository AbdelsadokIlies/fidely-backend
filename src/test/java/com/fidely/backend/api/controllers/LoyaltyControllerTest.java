package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.loyalties.LoyaltyResponse;
import com.fidely.backend.api.dtos.loyalties.LoyaltyTransactionResponse;
import com.fidely.backend.api.dtos.mappers.LoyaltyDtoMapper;
import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur REST de gestion des programmes de fidélité.
 *
 * <p>Ces tests vérifient le comportement HTTP du contrôleur
 * indépendamment de l'implémentation réelle des services.</p>
 */
@WebMvcTest(LoyaltyController.class)
class LoyaltyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ILoyaltyService loyaltyService;

    @MockitoBean
    private LoyaltyDtoMapper loyaltyDtoMapper;

    /**
     * Vérifie qu'un programme de fidélité peut être créé
     * à partir d'une requête valide.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldCreateLoyalty() throws Exception {

        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        LoyaltyResponse response = new LoyaltyResponse(
                loyaltyId,
                customerId,
                merchantId,
                0
        );

        when(loyaltyService.createLoyalty(
                customerId,
                merchantId
        )).thenReturn(loyalty);

        when(loyaltyDtoMapper.toResponse(loyalty))
                .thenReturn(response);

        String requestBody = """
                {
                    "customerId": "%s",
                    "merchantId": "%s"
                }
                """.formatted(
                customerId,
                merchantId
        );

        mockMvc.perform(
                        post("/api/loyalties")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance")
                        .value(0));
    }

    /**
     * Vérifie qu'une requête de création invalide
     * retourne une réponse HTTP 400.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {

        String requestBody = """
                {
                    "customerId": null,
                    "merchantId": null
                }
                """;

        mockMvc.perform(
                        post("/api/loyalties")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    /**
     * Vérifie qu'un programme de fidélité existant
     * peut être récupéré par son identifiant.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldGetLoyalty() throws Exception {

        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                100,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        LoyaltyResponse response = new LoyaltyResponse(
                loyaltyId,
                customerId,
                merchantId,
                100
        );

        when(loyaltyService.getLoyaltyById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(loyaltyDtoMapper.toResponse(loyalty))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/loyalties/{loyaltyId}", loyaltyId)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance")
                        .value(100));
    }

    /**
     * Vérifie qu'un programme de fidélité inexistant
     * retourne une réponse HTTP 404.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldReturn404WhenLoyaltyDoesNotExist() throws Exception {

        UUID loyaltyId = UUID.randomUUID();

        when(loyaltyService.getLoyaltyById(loyaltyId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/loyalties/{loyaltyId}", loyaltyId)
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Vérifie que les transactions d'un programme
     * de fidélité peuvent être récupérées.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldGetTransactionsByLoyalty() throws Exception {

        UUID loyaltyId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        LoyaltyTransaction transaction = new LoyaltyTransaction(
                transactionId,
                loyaltyId,
                ticketId,
                112,
                "Points gagnés sur le ticket TEST-001",
                LocalDateTime.now()
        );

        LoyaltyTransactionResponse response =
                new LoyaltyTransactionResponse(
                        transactionId,
                        loyaltyId,
                        ticketId,
                        112,
                        "Points gagnés sur le ticket TEST-001",
                        transaction.getCreatedAt()
                );

        when(loyaltyService.getTransactionsByLoyalty(loyaltyId))
                .thenReturn(List.of(transaction));

        when(loyaltyDtoMapper.toResponse(transaction))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/loyalties/{loyaltyId}/transactions",
                                loyaltyId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id")
                        .value(transactionId.toString()))
                .andExpect(jsonPath("$[0].loyaltyId")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$[0].ticketId")
                        .value(ticketId.toString()))
                .andExpect(jsonPath("$[0].points")
                        .value(112))
                .andExpect(jsonPath("$[0].description")
                        .value(
                                "Points gagnés sur le ticket TEST-001"
                        ));
    }

    /**
     * Vérifie qu'une transaction associée à un ticket
     * peut être récupérée.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldGetTransactionByTicket() throws Exception {

        UUID loyaltyId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        LoyaltyTransaction transaction = new LoyaltyTransaction(
                transactionId,
                loyaltyId,
                ticketId,
                112,
                "Points gagnés sur le ticket TEST-001",
                LocalDateTime.now()
        );

        LoyaltyTransactionResponse response =
                new LoyaltyTransactionResponse(
                        transactionId,
                        loyaltyId,
                        ticketId,
                        112,
                        "Points gagnés sur le ticket TEST-001",
                        transaction.getCreatedAt()
                );

        when(loyaltyService.getTransactionsByTicket(ticketId))
                .thenReturn(List.of(transaction));

        when(loyaltyDtoMapper.toResponse(transaction))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/loyalties/transactions/ticket/{ticketId}",
                                ticketId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id")
                        .value(transactionId.toString()))
                .andExpect(jsonPath("$.loyaltyId")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$.ticketId")
                        .value(ticketId.toString()))
                .andExpect(jsonPath("$.points")
                        .value(112));
    }

    /**
     * Vérifie qu'une image de ticket peut être envoyée
     * afin d'attribuer des points à une fidélité.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldEarnPointsFromTicketImage() throws Exception {

        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                112,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        LoyaltyResponse response = new LoyaltyResponse(
                loyaltyId,
                customerId,
                merchantId,
                112
        );

        byte[] imageBytes =
                "fake-ticket-image".getBytes();

        MockMultipartFile image =
                new MockMultipartFile(
                        "image",
                        "ticket.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        imageBytes
                );

        when(
                loyaltyService.addPointsFromTicket(
                        eq(loyaltyId),
                        any(byte[].class)
                )
        ).thenReturn(loyalty);

        when(loyaltyDtoMapper.toResponse(loyalty))
                .thenReturn(response);

        mockMvc.perform(
                        multipart(
                                "/api/loyalties/{loyaltyId}/earn",
                                loyaltyId
                        )
                                .file(image)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance")
                        .value(112));
    }

    /**
     * Vérifie qu'une requête d'attribution de points
     * sans image retourne une réponse HTTP 400.
     *
     * @throws Exception si l'appel HTTP échoue
     */
    @Test
    void shouldRejectEarnPointsRequestWithoutImage()
            throws Exception {

        UUID loyaltyId = UUID.randomUUID();

        mockMvc.perform(
                        multipart(
                                "/api/loyalties/{loyaltyId}/earn",
                                loyaltyId
                        )
                )
                .andExpect(status().isBadRequest());
    }
}