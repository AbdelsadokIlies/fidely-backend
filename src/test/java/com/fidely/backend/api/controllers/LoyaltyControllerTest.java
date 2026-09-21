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

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoyaltyController.class)
class LoyaltyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ILoyaltyService loyaltyService;

    @MockitoBean
    private LoyaltyDtoMapper loyaltyDtoMapper;

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
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
        );

        LoyaltyResponse response = new LoyaltyResponse(
                loyaltyId,
                customerId,
                merchantId,
                0
        );

        when(loyaltyService.createLoyalty(customerId, merchantId))
                .thenReturn(loyalty);

        when(loyaltyDtoMapper.toResponse(loyalty))
                .thenReturn(response);

        String requestBody = """
                {
                    "customerId": "%s",
                    "merchantId": "%s"
                }
                """.formatted(customerId, merchantId);

        mockMvc.perform(post("/api/loyalties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.merchantId").value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance").value(0));
    }

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
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
        );

        LoyaltyResponse response = new LoyaltyResponse(
                loyaltyId,
                customerId,
                merchantId,
                100
        );

        when(loyaltyService.getLoyaltyById(loyaltyId))
                .thenReturn(java.util.Optional.of(loyalty));

        when(loyaltyDtoMapper.toResponse(loyalty))
                .thenReturn(response);

        mockMvc.perform(get("/api/loyalties/{loyaltyId}", loyaltyId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.merchantId").value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance").value(100));
    }

    @Test
    void shouldReturn404WhenLoyaltyDoesNotExist() throws Exception {
        UUID loyaltyId = UUID.randomUUID();

        when(loyaltyService.getLoyaltyById(loyaltyId))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/loyalties/{loyaltyId}", loyaltyId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddPoints() throws Exception {
        UUID loyaltyId = UUID.randomUUID();

        doNothing()
                .when(loyaltyService)
                .addPoints(loyaltyId, 50);

        String requestBody = """
                {
                    "points": 50
                }
                """;

        mockMvc.perform(post(
                        "/api/loyalties/{loyaltyId}/points",
                        loyaltyId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRemovePoints() throws Exception {
        UUID loyaltyId = UUID.randomUUID();

        doNothing()
                .when(loyaltyService)
                .removePoints(loyaltyId, 30);

        String requestBody = """
                {
                    "points": 30
                }
                """;

        mockMvc.perform(post(
                        "/api/loyalties/{loyaltyId}/points/deduct",
                        loyaltyId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectInvalidPointsWhenAdding() throws Exception {
        UUID loyaltyId = UUID.randomUUID();

        String requestBody = """
                {
                    "points": 0
                }
                """;

        mockMvc.perform(post(
                        "/api/loyalties/{loyaltyId}/points",
                        loyaltyId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidPointsWhenRemoving() throws Exception {
        UUID loyaltyId = UUID.randomUUID();

        String requestBody = """
                {
                    "points": -10
                }
                """;

        mockMvc.perform(post(
                        "/api/loyalties/{loyaltyId}/points/deduct",
                        loyaltyId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        String requestBody = """
                {
                    "customerId": null,
                    "merchantId": null
                }
                """;

        mockMvc.perform(post("/api/loyalties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }@Test
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
                java.time.LocalDateTime.now()
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
                        get("/api/loyalties/{loyaltyId}/transactions", loyaltyId)
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
                        .value("Points gagnés sur le ticket TEST-001"));
    }

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
                java.time.LocalDateTime.now()
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
                        get("/api/loyalties/transactions/ticket/{ticketId}", ticketId)
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
}