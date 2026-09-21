package com.fidely.backend.application.Services;

import com.fidely.backend.application.TicketService;
import com.fidely.backend.application.port.out.ITicketRepository;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
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

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private ITicketRepository ticketRepository;

    private TicketService ticketService;

    private UUID ticketId;
    private UUID customerId;
    private UUID merchantId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository);

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
                "image-001",
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }


    @Test
    void shouldCreateTicket() {
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket result = ticketService.createTicket(ticket);

        assertThat(result).isSameAs(ticket);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldCreateTicketFromOcr() {
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket result = ticketService.createTicketFromOcr(ticket);

        assertThat(result).isSameAs(ticket);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldGetTicketById() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Optional<Ticket> result = ticketService.getTicketById(ticketId);

        assertThat(result).containsSame(ticket);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldGetTicketByNumber() {
        when(ticketRepository.findByTicketNumber("TICKET-001"))
                .thenReturn(Optional.of(ticket));

        Optional<Ticket> result = ticketService.getTicketByNumber("TICKET-001");

        assertThat(result).containsSame(ticket);
        verify(ticketRepository).findByTicketNumber("TICKET-001");
    }

    @Test
    void shouldGetTicketsByCustomer() {
        when(ticketRepository.findByCustomerId(customerId))
                .thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getTicketsByCustomer(customerId);

        assertThat(result).containsExactly(ticket);
        verify(ticketRepository).findByCustomerId(customerId);
    }

    @Test
    void shouldGetTicketsByMerchant() {
        when(ticketRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getTicketsByMerchant(merchantId);

        assertThat(result).containsExactly(ticket);
        verify(ticketRepository).findByMerchantId(merchantId);
    }

    @Test
    void shouldGetTicketsByCustomerAndMerchant() {
        when(ticketRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        )).thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getTicketsByCustomerAndMerchant(
                customerId,
                merchantId
        );

        assertThat(result).containsExactly(ticket);
        verify(ticketRepository).findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        );
    }

    @Test
    void shouldValidateTicket() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketService.validateTicket(ticketId);

        assertThat(ticket.isValidated()).isTrue();
        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldRejectTicket() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketService.rejectTicket(ticketId, "Ticket illisible");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.REJECTED);
        assertThat(ticket.getRejectionReason()).isEqualTo("Ticket illisible");

        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldMarkTicketAsDuplicate() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketService.markAsDuplicate(ticketId);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.DUPLICATE);

        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldThrowWhenTicketDoesNotExist() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.validateTicket(ticketId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ticket introuvable");

        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository, never()).save(any());
    }
}