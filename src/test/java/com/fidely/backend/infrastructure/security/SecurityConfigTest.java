package com.fidely.backend.infrastructure.security;

import com.fidely.backend.api.controllers.TransactionController;
import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketResponseMapper;
import com.fidely.backend.api.dtos.mappers.tickets.TicketResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionRequestMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.ManualTransactionRequestMapper;
import com.fidely.backend.api.dtos.models.tickets.TicketResponse;
import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.security.IAccessTokenManagement;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    private static final String ACCESS_TOKEN_COOKIE =
            "fidely_access_token";

    private static final String ACCESS_TOKEN =
            "valid-access-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAccessTokenManagement accessTokenManagement;

    @MockitoBean
    private ITicketService ticketService;

    @MockitoBean
    private ILoyaltyService loyaltyService;

    @MockitoBean
    private OcrTicketResponseMapper ocrTicketResponseMapper;

    @MockitoBean
    private TicketResponseMapper ticketResponseMapper;

    @MockitoBean
    private CreateTransactionRequestMapper createTransactionRequestMapper;

    @MockitoBean
    private CreateTransactionResponseMapper createTransactionResponseMapper;

    @MockitoBean
    private ManualTransactionRequestMapper manualTransactionRequestMapper;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        UUID transactionId = UUID.randomUUID();

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedRequest() throws Exception {

        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(accessTokenManagement.extractUserId(ACCESS_TOKEN))
                .thenReturn(userId);

        when(accessTokenManagement.extractRole(ACCESS_TOKEN))
                .thenReturn("CUSTOMER");

        Ticket ticket = mock(Ticket.class);
        TicketResponse ticketResponse = mock(TicketResponse.class);

        when(ticketService.getTicketById(transactionId))
                .thenReturn(Optional.of(ticket));

        when(ticketResponseMapper.toResponse(ticket))
                .thenReturn(ticketResponse);

        mockMvc.perform(
                        get("/transactions/{id}", transactionId)
                                .cookie(
                                        new Cookie(
                                                ACCESS_TOKEN_COOKIE,
                                                ACCESS_TOKEN
                                        )
                                )
                )
                .andExpect(status().isOk());
    }
}