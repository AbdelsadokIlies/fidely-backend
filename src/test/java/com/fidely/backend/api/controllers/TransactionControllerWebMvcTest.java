package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionRequestMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionResponseMapper;
import com.fidely.backend.api.dtos.models.tickets.OcrTicketResponse;
import com.fidely.backend.api.dtos.models.transactions.CreateTransactionRequest;
import com.fidely.backend.api.dtos.models.transactions.CreateTransactionResponse;
import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**

 * Tests web du controller REST des transactions.
 *
 * <p>Ces tests vérifient le comportement HTTP des endpoints de transaction,
 * notamment le binding des requêtes multipart et JSON ainsi que la
 * sérialisation des réponses.</p>
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITicketService ticketService;

    @MockitoBean
    private ILoyaltyService loyaltyService;

    @MockitoBean
    private OcrTicketResponseMapper ocrTicketResponseMapper;

    @MockitoBean
    private CreateTransactionRequestMapper createTransactionRequestMapper;

    @MockitoBean
    private CreateTransactionResponseMapper createTransactionResponseMapper;

    /**

     * Vérifie que le endpoint OCR accepte une requête multipart
     * valide et retourne les données extraites au format JSON.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldExtractTicketFromOcr() throws Exception {
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
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.merchantId").value(merchantId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.ticketNumber").value("TICKET-001"))
                .andExpect(jsonPath("$.amount").value(112.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    /**

     * Vérifie que le endpoint de création d'une transaction
     * accepte une requête JSON valide et retourne la fidélité
     * mise à jour.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldCreateTransaction() throws Exception {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        CreateTransactionRequest request = new CreateTransactionRequest(
                loyaltyId,
                merchantId,
                customerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT"
        );

        Ticket ticket = new Ticket(
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
        );

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                112,
                LocalDateTime.of(2026, 6, 15, 12, 30),
                LocalDateTime.of(2026, 6, 15, 12, 31)
        );

        CreateTransactionResponse response = new CreateTransactionResponse(
                loyaltyId,
                customerId,
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
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/json"
                ))
                .andExpect(jsonPath("$.loyaltyId")
                        .value(loyaltyId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.pointsBalance")
                        .value(112));
    }

    /**

     * Vérifie que le endpoint de création d'une transaction
     * retourne une erreur HTTP 400 lorsqu'une donnée obligatoire
     * est absente de la requête.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldRejectInvalidTransactionRequest() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(
                        post("/transactions")
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
                        "application/json"
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
    /**

     * Vérifie que le endpoint de création d'une transaction
     * retourne une erreur HTTP 400 lorsqu'une erreur métier
     * de type {@link IllegalArgumentException} est levée.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldReturnBadRequestWhenTransactionCreationFails() throws Exception {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

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
                        "application/json"
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Loyalty not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    /**

     * Vérifie que le endpoint de création d'une transaction
     * retourne une erreur HTTP 409 lorsqu'un conflit métier
     * de type {@link IllegalStateException} est levé.
     *
     * @throws Exception si l'exécution de la requête HTTP échoue
     */
    @Test
    void shouldReturnConflictWhenTransactionCreationFails() throws Exception {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

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
                new IllegalStateException("Ce ticket a déjà été utilisé.")
        );

        mockMvc.perform(
                        post("/transactions")
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
                        "application/json"
                ))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Ce ticket a déjà été utilisé."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
