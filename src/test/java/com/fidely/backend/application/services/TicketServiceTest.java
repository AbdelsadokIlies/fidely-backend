package com.fidely.backend.application.services;

import com.fidely.backend.application.TicketService;
import com.fidely.backend.application.port.out.IOcrService;
import com.fidely.backend.application.port.out.ITicketRepository;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import com.fidely.backend.infrastructure.ocr.OcrTicketData;
import com.fidely.backend.infrastructure.ocr.OcrTicketMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service de gestion des tickets.
 *
 * <p>Les dépendances externes du service sont simulées avec Mockito
 * afin de tester uniquement le comportement de {@link TicketService}.</p>
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private ITicketRepository ticketRepository;

    @Mock
    private IOcrService ocrService;

    @Mock
    private OcrTicketMapper ocrTicketMapper;

    private TicketService ticketService;

    private UUID ticketId;
    private UUID customerId;
    private UUID merchantId;

    private Ticket ticket;

    /**
     * Initialise les données communes utilisées par les tests.
     */
    @BeforeEach
    void setUp() {
        ticketService = new TicketService(
                ticketRepository,
                ocrService,
                ocrTicketMapper
        );

        ticketId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        merchantId = UUID.randomUUID();

        ticket = new Ticket(
                ticketId,
                merchantId,
                customerId,
                "TICKET-001",
                "fingerprint-123",
                LocalDate.of(2026, 9, 20),
                LocalTime.of(14, 30),
                new BigDecimal("42.50"),
                "OCR TEXT",
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Vérifie qu'un ticket peut être créé et sauvegardé.
     */
    @Test
    void shouldCreateTicket() {
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket result = ticketService.createTicket(ticket);

        assertThat(result).isSameAs(ticket);

        verify(ticketRepository).save(ticket);
    }

    /**
     * Vérifie qu'un ticket peut être créé à partir d'une image
     * en passant par le service OCR puis le mapper.
     *
     * <p>Le fingerprint est calculé automatiquement par le service
     * à partir des données du ticket OCR.</p>
     */
    @Test
    void shouldCreateTicketFromOcr() {
        byte[] image = "fake-image".getBytes();

        OcrTicketData ocrData = new OcrTicketData(
                ticket.getTicketNumber(),
                ticket.getTicketDate(),
                ticket.getTicketTime(),
                ticket.getAmount(),
                ticket.getRawOcrText()
        );

        when(ocrService.extractTicketData(image))
                .thenReturn(ocrData);

        when(ticketRepository.existsByFingerprintHash(anyString()))
                .thenReturn(false);

        when(ocrTicketMapper.toDomain(
                eq(ocrData),
                eq(merchantId),
                eq(customerId),
                anyString()
        )).thenReturn(ticket);

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        Ticket result = ticketService.createTicketFromOcr(
                image,
                merchantId,
                customerId
        );

        assertThat(result).isSameAs(ticket);

        verify(ocrService)
                .extractTicketData(image);

        verify(ticketRepository)
                .existsByFingerprintHash(anyString());

        verify(ocrTicketMapper).toDomain(
                eq(ocrData),
                eq(merchantId),
                eq(customerId),
                anyString()
        );

        verify(ticketRepository)
                .save(ticket);
    }

    /**
     * Vérifie qu'une exception est levée lorsqu'un ticket
     * ayant déjà été utilisé est soumis à nouveau.
     */
    @Test
    void shouldRejectDuplicateTicketFromOcr() {
        byte[] image = "fake-image".getBytes();

        OcrTicketData ocrData = new OcrTicketData(
                ticket.getTicketNumber(),
                ticket.getTicketDate(),
                ticket.getTicketTime(),
                ticket.getAmount(),
                ticket.getRawOcrText()
        );

        when(ocrService.extractTicketData(image))
                .thenReturn(ocrData);

        when(ticketRepository.existsByFingerprintHash(anyString()))
                .thenReturn(true);

        assertThatThrownBy(
                () -> ticketService.createTicketFromOcr(
                        image,
                        merchantId,
                        customerId
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ce ticket a déjà été utilisé.");

        verify(ocrService)
                .extractTicketData(image);

        verify(ticketRepository)
                .existsByFingerprintHash(anyString());

        verify(ocrTicketMapper, never())
                .toDomain(
                        any(),
                        any(),
                        any(),
                        anyString()
                );

        verify(ticketRepository, never())
                .save(any());
    }

    /**
     * Vérifie qu'un ticket peut être récupéré à partir de son identifiant.
     */
    @Test
    void shouldGetTicketById() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        Optional<Ticket> result = ticketService.getTicketById(ticketId);

        assertThat(result).containsSame(ticket);

        verify(ticketRepository).findById(ticketId);
    }

    /**
     * Vérifie qu'un ticket peut être récupéré à partir de son numéro.
     */
    @Test
    void shouldGetTicketByNumber() {
        when(ticketRepository.findByTicketNumber("TICKET-001"))
                .thenReturn(Optional.of(ticket));

        Optional<Ticket> result =
                ticketService.getTicketByNumber("TICKET-001");

        assertThat(result).containsSame(ticket);

        verify(ticketRepository)
                .findByTicketNumber("TICKET-001");
    }

    /**
     * Vérifie que les tickets d'un client peuvent être récupérés.
     */
    @Test
    void shouldGetTicketsByCustomer() {
        when(ticketRepository.findByCustomerId(customerId))
                .thenReturn(List.of(ticket));

        List<Ticket> result =
                ticketService.getTicketsByCustomer(customerId);

        assertThat(result).containsExactly(ticket);

        verify(ticketRepository)
                .findByCustomerId(customerId);
    }

    /**
     * Vérifie que les tickets d'un marchand peuvent être récupérés.
     */
    @Test
    void shouldGetTicketsByMerchant() {
        when(ticketRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(ticket));

        List<Ticket> result =
                ticketService.getTicketsByMerchant(merchantId);

        assertThat(result).containsExactly(ticket);

        verify(ticketRepository)
                .findByMerchantId(merchantId);
    }

    /**
     * Vérifie que les tickets d'un client chez un marchand donné
     * peuvent être récupérés.
     */
    @Test
    void shouldGetTicketsByCustomerAndMerchant() {
        when(ticketRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        )).thenReturn(List.of(ticket));

        List<Ticket> result =
                ticketService.getTicketsByCustomerAndMerchant(
                        customerId,
                        merchantId
                );

        assertThat(result).containsExactly(ticket);

        verify(ticketRepository)
                .findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                );
    }

    /**
     * Vérifie qu'un ticket en attente peut être validé.
     */
    @Test
    void shouldValidateTicket() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        ticketService.validateTicket(ticketId);

        assertThat(ticket.isValidated()).isTrue();

        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    /**
     * Vérifie qu'un ticket en attente peut être rejeté
     * avec une raison de rejet.
     */
    @Test
    void shouldRejectTicket() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        ticketService.rejectTicket(
                ticketId,
                "Ticket illisible"
        );

        assertThat(ticket.getStatus())
                .isEqualTo(TicketStatus.REJECTED);

        assertThat(ticket.getRejectionReason())
                .isEqualTo("Ticket illisible");

        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    /**
     * Vérifie qu'un ticket en attente peut être marqué comme doublon.
     */
    @Test
    void shouldMarkTicketAsDuplicate() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        ticketService.markAsDuplicate(ticketId);

        assertThat(ticket.getStatus())
                .isEqualTo(TicketStatus.DUPLICATE);

        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    /**
     * Vérifie qu'une exception est levée lorsqu'un ticket
     * recherché pour validation n'existe pas.
     */
    @Test
    void shouldThrowWhenTicketDoesNotExist() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> ticketService.validateTicket(ticketId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ticket introuvable");

        verify(ticketRepository).findById(ticketId);

        verify(ticketRepository, never())
                .save(any());
    }
}