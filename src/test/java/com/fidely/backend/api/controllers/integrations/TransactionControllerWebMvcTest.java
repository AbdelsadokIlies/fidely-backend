package com.fidely.backend.api.controllers.integrations;

import com.fidely.backend.api.controllers.TransactionController;
import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionRequestMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.LoyaltyTransactionResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.ManualTransactionRequestMapper;
import com.fidely.backend.api.dtos.mappers.transactions.TicketResponseMapper;
import com.fidely.backend.api.dtos.models.tickets.OcrTicketResponse;
import com.fidely.backend.api.dtos.models.tickets.TicketResponse;
import com.fidely.backend.api.dtos.models.transactions.CreateTransactionRequest;
import com.fidely.backend.api.dtos.models.transactions.CreateTransactionResponse;
import com.fidely.backend.api.dtos.models.transactions.LoyaltyTransactionResponse;
import com.fidely.backend.api.dtos.models.transactions.ManualTransactionRequest;
import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.security.IAccessTokenManagement;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import com.fidely.backend.domain.models.users.MerchantManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests web du controller REST des transactions.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerWebMvcTest {

    private static final String ACCESS_TOKEN_COOKIE =
            "fidely_access_token";

    private static final String ACCESS_TOKEN =
            "test-access-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITicketService ticketService;

    @MockitoBean
    private ILoyaltyService loyaltyService;

    @MockitoBean
    private IUserRepository userRepository;

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

    @MockitoBean
    private IAccessTokenManagement accessTokenManagement;

    @MockitoBean
    private LoyaltyTransactionResponseMapper loyaltyTransactionResponseMapper;

    @MockitoBean
    private MerchantManager merchantManager;

    /**
     * Vérifie que le endpoint OCR accepte une requête multipart
     * valide et retourne les données extraites au format JSON.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldExtractTicketFromOcr() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticateAsCustomer(userId);

        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        byte[] imageContent = "fake-image".getBytes();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "ticket.jpg",
                "image/jpeg",
                imageContent
        );

        OcrTicketResponse response = new OcrTicketResponse(
                ticketId,
                merchantId,
                customerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.PENDING
        );

        when(ticketService.extractTicketFromOcr(
                any(byte[].class),
                eq(merchantId),
                eq(customerId)
        )).thenReturn(
                new Ticket(
                        ticketId,
                        merchantId,
                        customerId,
                        "TICKET-001",
                        LocalDate.of(2026, 6, 15),
                        LocalTime.of(12, 30),
                        new BigDecimal("112.00"),
                        "OCR TEXT",
                        TicketStatus.PENDING,
                        null,
                        LocalDateTime.of(2026, 6, 15, 12, 30)
                )
        );

        when(ocrTicketResponseMapper.toResponse(any()))
                .thenReturn(response);

        mockMvc.perform(
                        multipart("/transactions/ocr")
                                .file(image)
                                .param("merchantId", merchantId.toString())
                                .param("customerId", customerId.toString())
                                .cookie(accessTokenCookie())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.id")
                        .value(ticketId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.ticketNumber")
                        .value("TICKET-001"))
                .andExpect(jsonPath("$.amount")
                        .value(112.00))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));
    }

    /**
     * Vérifie que le endpoint de création d'une transaction
     * accepte une requête JSON valide.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldCreateTransaction() throws Exception {
        UUID customerId = UUID.randomUUID();

        authenticateAsCustomer(customerId);

        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID ticketCustomerId = customerId;
        UUID ticketId = UUID.randomUUID();

        Ticket ticket = new Ticket(
                ticketId,
                merchantId,
                ticketCustomerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.PENDING,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                ticketCustomerId,
                merchantId,
                112,
                LocalDateTime.of(2026, 6, 15, 12, 30),
                LocalDateTime.of(2026, 6, 15, 12, 31)
        );

        CreateTransactionResponse response =
                new CreateTransactionResponse(
                        loyaltyId,
                        ticketCustomerId,
                        merchantId,
                        112,
                        loyalty.getUpdatedAt()
                );

        when(createTransactionRequestMapper.toTicket(any(
                CreateTransactionRequest.class
        ))).thenReturn(ticket);

        when(loyaltyService.addPointsFromTicket(
                eq(loyaltyId),
                eq(ticket)
        )).thenReturn(loyalty);

        when(createTransactionResponseMapper.toResponse(eq(loyalty)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/transactions")
                                .principal(createAuthentication(customerId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": "%s",
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "ticketNumber": "TICKET-001",
                                    "ticketDate": "2026-06-15",
                                    "ticketTime": "12:30:00",
                                    "amount": 112.00,
                                    "rawOcrText": "OCR TEXT"
                                    }
                                    """.formatted(
                                        loyaltyId,
                                        merchantId,
                                        ticketCustomerId
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.loyaltyId")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(ticketCustomerId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance")
                        .value(112));
    }

    /**
     * Vérifie qu'une requête de transaction invalide
     * retourne HTTP 400.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldRejectInvalidTransactionRequest() throws Exception {
        UUID customerId = UUID.randomUUID();

        authenticateAsCustomer(customerId);

        UUID merchantId = UUID.randomUUID();

        mockMvc.perform(
                        post("/transactions")
                                .principal(createAuthentication(customerId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": null,
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "ticketNumber": "",
                                    "ticketDate": null,
                                    "ticketTime": null,
                                    "amount": 0
                                    }
                                    """.formatted(
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Vérifie qu'une IllegalArgumentException retourne HTTP 400.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldReturnBadRequestWhenTransactionCreationFails()
            throws Exception {

        UUID customerId = UUID.randomUUID();

        authenticateAsCustomer(customerId);

        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.PENDING,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );

        when(createTransactionRequestMapper.toTicket(any(
                CreateTransactionRequest.class
        ))).thenReturn(ticket);

        when(loyaltyService.addPointsFromTicket(
                eq(loyaltyId),
                eq(ticket)
        )).thenThrow(
                new IllegalArgumentException("Loyalty not found")
        );

        mockMvc.perform(
                        post("/transactions")
                                .principal(createAuthentication(customerId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": "%s",
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "ticketNumber": "TICKET-001",
                                    "ticketDate": "2026-06-15",
                                    "ticketTime": "12:30:00",
                                    "amount": 112.00,
                                    "rawOcrText": "OCR TEXT"
                                    }
                                    """.formatted(
                                        loyaltyId,
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Loyalty not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Vérifie qu'une IllegalStateException retourne HTTP 409.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldReturnConflictWhenTransactionCreationFails()
            throws Exception {

        UUID customerId = UUID.randomUUID();

        authenticateAsCustomer(customerId);

        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.PENDING,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );

        when(createTransactionRequestMapper.toTicket(any(
                CreateTransactionRequest.class
        ))).thenReturn(ticket);

        when(loyaltyService.addPointsFromTicket(
                eq(loyaltyId),
                eq(ticket)
        )).thenThrow(
                new IllegalStateException(
                        "Ce ticket a déjà été utilisé."
                )
        );

        mockMvc.perform(
                        post("/transactions")
                                .principal(createAuthentication(customerId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": "%s",
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "ticketNumber": "TICKET-001",
                                    "ticketDate": "2026-06-15",
                                    "ticketTime": "12:30:00",
                                    "amount": 112.00,
                                    "rawOcrText": "OCR TEXT"
                                    }
                                    """.formatted(
                                        loyaltyId,
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Ce ticket a déjà été utilisé."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Vérifie qu'un marchand authentifié peut créer
     * une transaction manuelle.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldCreateManualTransactionAsMerchant() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        authenticateAsMerchant(userId, merchantId);

        ManualTransactionRequest request =
                new ManualTransactionRequest(
                        loyaltyId,
                        merchantId,
                        customerId,
                        new BigDecimal("112.00")
                );

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                112,
                LocalDateTime.of(2026, 6, 15, 12, 30),
                LocalDateTime.of(2026, 6, 15, 12, 31)
        );

        CreateTransactionResponse response =
                new CreateTransactionResponse(
                        loyaltyId,
                        customerId,
                        merchantId,
                        112,
                        loyalty.getUpdatedAt()
                );

        when(manualTransactionRequestMapper.toRequest(
                any(ManualTransactionRequest.class)
        )).thenReturn(request);

        when(loyaltyService.addPointsManually(
                eq(loyaltyId),
                eq(merchantId),
                eq(customerId),
                eq(new BigDecimal("112.00"))
        )).thenReturn(loyalty);

        when(createTransactionResponseMapper.toResponse(eq(loyalty)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/transactions/manual")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": "%s",
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "amount": 112.00
                                    }
                                    """.formatted(
                                        loyaltyId,
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.loyaltyId")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance")
                        .value(112));

        verify(loyaltyService).addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                new BigDecimal("112.00")
        );
    }

    /**
     * Vérifie qu'un client authentifié ne peut pas créer
     * une transaction manuelle.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldRejectManualTransactionForCustomer() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();

        authenticateAsCustomer(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/transactions/manual")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": "%s",
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "amount": 112.00
                                    }
                                    """.formatted(
                                        loyaltyId,
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "L'utilisateur authentifié n'est pas un marchand."
                        ))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Vérifie qu'un marchand ne peut pas créer une transaction
     * pour un autre marchand.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldRejectManualTransactionForAnotherMerchant() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID authenticatedMerchantId = UUID.randomUUID();
        UUID requestedMerchantId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        authenticateAsMerchant(
                userId,
                authenticatedMerchantId
        );

        ManualTransactionRequest request =
                new ManualTransactionRequest(
                        loyaltyId,
                        requestedMerchantId,
                        customerId,
                        new BigDecimal("112.00")
                );

        when(manualTransactionRequestMapper.toRequest(
                any(ManualTransactionRequest.class)
        )).thenReturn(request);

        mockMvc.perform(
                        post("/transactions/manual")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                "loyaltyId": "%s",
                                "merchantId": "%s",
                                "customerId": "%s",
                                "amount": 112.00
                                }
                                """.formatted(
                                        loyaltyId,
                                        requestedMerchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Le marchand fourni ne correspond pas "
                                        + "au marchand authentifié."
                        ))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(loyaltyService, never()).addPointsManually(
                any(UUID.class),
                any(UUID.class),
                any(UUID.class),
                any(BigDecimal.class)
        );
    }

    /**
     * Vérifie qu'une requête manuelle invalide retourne HTTP 400.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldRejectInvalidManualTransactionRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        authenticateAsMerchant(
                userId,
                merchantId
        );

        mockMvc.perform(
                        post("/transactions/manual")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": null,
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "amount": 0
                                    }
                                    """.formatted(
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Vérifie qu'une IllegalArgumentException retourne HTTP 400.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldReturnBadRequestWhenManualTransactionCreationFails()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        authenticateAsMerchant(userId, merchantId);

        ManualTransactionRequest request =
                new ManualTransactionRequest(
                        loyaltyId,
                        merchantId,
                        customerId,
                        new BigDecimal("112.00")
                );

        when(manualTransactionRequestMapper.toRequest(
                any(ManualTransactionRequest.class)
        )).thenReturn(request);

        when(loyaltyService.addPointsManually(
                eq(loyaltyId),
                eq(merchantId),
                eq(customerId),
                eq(new BigDecimal("112.00"))
        )).thenThrow(
                new IllegalArgumentException("Loyalty introuvable")
        );

        mockMvc.perform(
                        post("/transactions/manual")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": "%s",
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "amount": 112.00
                                    }
                                    """.formatted(
                                        loyaltyId,
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Loyalty introuvable"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Vérifie qu'une IllegalStateException retourne HTTP 409.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldReturnConflictWhenManualTransactionCreationFails()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        authenticateAsMerchant(userId, merchantId);

        ManualTransactionRequest request =
                new ManualTransactionRequest(
                        loyaltyId,
                        merchantId,
                        customerId,
                        new BigDecimal("112.00")
                );

        when(manualTransactionRequestMapper.toRequest(
                any(ManualTransactionRequest.class)
        )).thenReturn(request);

        when(loyaltyService.addPointsManually(
                eq(loyaltyId),
                eq(merchantId),
                eq(customerId),
                eq(new BigDecimal("112.00"))
        )).thenThrow(
                new IllegalStateException(
                        "La transaction ne génère aucun point."
                )
        );

        mockMvc.perform(
                        post("/transactions/manual")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                    "loyaltyId": "%s",
                                    "merchantId": "%s",
                                    "customerId": "%s",
                                    "amount": 112.00
                                    }
                                    """.formatted(
                                        loyaltyId,
                                        merchantId,
                                        customerId
                                ))
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "La transaction ne génère aucun point."
                        ))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Vérifie que le endpoint de récupération d'un ticket
     * retourne HTTP 200 lorsque le ticket existe.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldGetTicketById() throws Exception {
        UUID customerId = UUID.randomUUID();

        authenticateAsCustomer(customerId);

        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Ticket ticket = new Ticket(
                ticketId,
                merchantId,
                customerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.VALIDATED,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );

        TicketResponse response = new TicketResponse(
                ticketId,
                merchantId,
                customerId,
                "TICKET-001",
                ticket.getFingerprintHash(),
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.VALIDATED,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketResponseMapper.toResponse(ticket))
                .thenReturn(response);

        mockMvc.perform(
                        get("/transactions/{ticketId}", ticketId)
                                .principal(createAuthentication(customerId))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.id")
                        .value(ticketId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.ticketNumber")
                        .value("TICKET-001"))
                .andExpect(jsonPath("$.amount")
                        .value(112.00))
                .andExpect(jsonPath("$.status")
                        .value("VALIDATED"));
    }

    /**
     * Vérifie que le endpoint de récupération d'un ticket
     * retourne HTTP 404 lorsque le ticket n'existe pas.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldReturnNotFoundWhenTicketDoesNotExist() throws Exception {
        UUID customerId = UUID.randomUUID();

        authenticateAsCustomer(customerId);

        UUID ticketId = UUID.randomUUID();

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/transactions/{ticketId}", ticketId)
                                .principal(createAuthentication(customerId))
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Vérifie que le endpoint retourne toutes les transactions
     * du client authentifié.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldGetCustomerTransactions() throws Exception {
        UUID customerId = UUID.randomUUID();

        LoyaltyTransaction transaction = new LoyaltyTransaction(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                25,
                "Achat",
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );

        LoyaltyTransactionResponse response =
                new LoyaltyTransactionResponse(
                        transaction.getId(),
                        transaction.getLoyaltyId(),
                        transaction.getTicketId(),
                        transaction.getPoints(),
                        transaction.getDescription(),
                        transaction.getCreatedAt()
                );

        when(loyaltyService.getTransactionsByCustomer(customerId))
                .thenReturn(List.of(transaction));

        when(loyaltyTransactionResponseMapper.toResponse(transaction))
                .thenReturn(response);

        mockMvc.perform(
                        get("/transactions/customers/me")
                                .principal(createAuthentication(customerId))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(transaction.getId().toString()))
                .andExpect(jsonPath("$[0].loyaltyId")
                        .value(transaction.getLoyaltyId().toString()))
                .andExpect(jsonPath("$[0].ticketId")
                        .value(transaction.getTicketId().toString()))
                .andExpect(jsonPath("$[0].points")
                        .value(25))
                .andExpect(jsonPath("$[0].description")
                        .value("Achat"));

        verify(loyaltyService)
                .getTransactionsByCustomer(customerId);

        verify(loyaltyTransactionResponseMapper)
                .toResponse(transaction);
    }

    /**
     * Vérifie que le endpoint retourne toutes les transactions
     * du marchand authentifié.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldGetMerchantTransactions() throws Exception {
        UUID merchantId = UUID.randomUUID();

        LoyaltyTransaction transaction = new LoyaltyTransaction(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                30,
                "Achat",
                LocalDateTime.of(2026, 6, 15, 14, 30)
        );

        LoyaltyTransactionResponse response =
                new LoyaltyTransactionResponse(
                        transaction.getId(),
                        transaction.getLoyaltyId(),
                        transaction.getTicketId(),
                        transaction.getPoints(),
                        transaction.getDescription(),
                        transaction.getCreatedAt()
                );

        when(loyaltyService.getTransactionsByMerchant(merchantId))
                .thenReturn(List.of(transaction));

        when(loyaltyTransactionResponseMapper.toResponse(transaction))
                .thenReturn(response);

        mockMvc.perform(
                        get("/transactions/merchants/me")
                                .principal(createAuthentication(merchantId))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(transaction.getId().toString()))
                .andExpect(jsonPath("$[0].loyaltyId")
                        .value(transaction.getLoyaltyId().toString()))
                .andExpect(jsonPath("$[0].ticketId")
                        .value(transaction.getTicketId().toString()))
                .andExpect(jsonPath("$[0].points")
                        .value(30))
                .andExpect(jsonPath("$[0].description")
                        .value("Achat"));

        verify(loyaltyService)
                .getTransactionsByMerchant(merchantId);

        verify(loyaltyTransactionResponseMapper)
                .toResponse(transaction);
    }

    /**
     * Authentifie la requête en tant que marchand.
     *
     * @param userId identifiant de l'utilisateur
     * @param merchantId identifiant du marchand
     */
    private void authenticateAsMerchant(
            UUID userId,
            UUID merchantId
    ) {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(merchantManager.getMerchantId())
                .thenReturn(merchantId);
    }

    /**
     * Authentifie la requête en tant que client.
     *
     * @param userId identifiant de l'utilisateur
     */
    private void authenticateAsCustomer(UUID userId) {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
    }

    /**
     * Crée une authentification simulée contenant l'identifiant
     * utilisateur attendu par le controller.
     *
     * @param userId identifiant de l'utilisateur
     * @return authentification simulée
     */
    private Authentication createAuthentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null
        );
    }

    /**
     * Construit le cookie contenant l'access token simulé.
     *
     * @return cookie d'authentification
     */
    private Cookie accessTokenCookie() {
        return new Cookie(
                ACCESS_TOKEN_COOKIE,
                ACCESS_TOKEN
        );
    }
}