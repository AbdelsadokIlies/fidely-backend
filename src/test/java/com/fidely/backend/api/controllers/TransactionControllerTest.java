package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketRequest;
import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketResponseMapper;
import com.fidely.backend.api.dtos.models.tickets.OcrTicketResponse;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du controller REST des transactions.
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private ITicketService ticketService;

    @Mock
    private OcrTicketResponseMapper ocrTicketResponseMapper;

    @InjectMocks
    private TransactionController transactionController;

    /**
     * Vérifie que le controller transmet correctement les données
     * au service OCR et retourne la réponse correspondante.
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
                imageContent,
                merchantId,
                customerId
        )).thenReturn(ticket);

        when(ocrTicketResponseMapper.toResponse(ticket))
                .thenReturn(response);

        // On construit directement la requête via le DTO.
        // Le comportement multipart sera testé avec MockMvc ensuite.
        OcrTicketRequest request =
                new OcrTicketRequest(
                        merchantId,
                        customerId,
                        image
                );

        ResponseEntity<OcrTicketResponse> result =
                transactionController.extractTicketFromOcr(request);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isSameAs(response);

        verify(ticketService).extractTicketFromOcr(
                imageContent,
                merchantId,
                customerId
        );

        verify(ocrTicketResponseMapper).toResponse(ticket);
    }
}