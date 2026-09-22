package com.fidely.backend.domain.models.tickets;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private final UUID id = UUID.randomUUID();
    private final UUID merchantId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final String ticketNumber = "TICKET-123";
    private final LocalDate ticketDate = LocalDate.of(2026, 9, 20);
    private final LocalTime ticketTime = LocalTime.of(14, 30);
    private final BigDecimal amount = new BigDecimal("42.50");
    private final String rawOcrText = "Ticket de caisse";
    private final LocalDateTime createdAt =
            LocalDateTime.of(2026, 9, 20, 15, 0);

    @Test
    void shouldCreateValidTicket() {
        Ticket ticket = createTicket();

        assertNotNull(ticket);
        assertEquals(id, ticket.getId());
        assertEquals(merchantId, ticket.getMerchantId());
        assertEquals(customerId, ticket.getCustomerId());
        assertEquals(ticketNumber, ticket.getTicketNumber());
        assertNotNull(ticket.getFingerprintHash());
        assertFalse(ticket.getFingerprintHash().isBlank());
        assertEquals(ticketDate, ticket.getTicketDate());
        assertEquals(ticketTime, ticket.getTicketTime());
        assertEquals(amount, ticket.getAmount());
        assertEquals(rawOcrText, ticket.getRawOcrText());
        assertEquals(TicketStatus.PENDING, ticket.getStatus());
        assertNull(ticket.getRejectionReason());
        assertEquals(createdAt, ticket.getCreatedAt());
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicket(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicket(new BigDecimal("-10.00"))
        );
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicket(null)
        );
    }

    @Test
    void shouldRejectNullMerchant() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Ticket(
                        id,
                        null,
                        customerId,
                        ticketNumber,
                        ticketDate,
                        ticketTime,
                        amount,
                        rawOcrText,
                        TicketStatus.PENDING,
                        null,
                        createdAt
                )
        );
    }

    @Test
    void shouldRejectNullCustomer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Ticket(
                        id,
                        merchantId,
                        null,
                        ticketNumber,
                        ticketDate,
                        ticketTime,
                        amount,
                        rawOcrText,
                        TicketStatus.PENDING,
                        null,
                        createdAt
                )
        );
    }

    @Test
    void shouldRejectNullTicketNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithTicketNumber(null)
        );
    }

    @Test
    void shouldRejectBlankTicketNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithTicketNumber("   ")
        );
    }

    @Test
    void shouldRejectNullTicketDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithTicketDate(null)
        );
    }

    @Test
    void shouldRejectNullTicketTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithTicketTime(null)
        );
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithStatus(null, null)
        );
    }

    @Test
    void shouldRejectNullCreatedAt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithCreatedAt(null)
        );
    }

    @Test
    void shouldRejectRejectionReasonForPendingTicket() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithStatus(
                        TicketStatus.PENDING,
                        "Some reason"
                )
        );
    }

    @Test
    void shouldRejectRejectionReasonForValidatedTicket() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithStatus(
                        TicketStatus.VALIDATED,
                        "Some reason"
                )
        );
    }

    @Test
    void shouldRejectRejectionReasonForDuplicateTicket() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithStatus(
                        TicketStatus.DUPLICATE,
                        "Some reason"
                )
        );
    }

    @Test
    void shouldRequireRejectionReasonForRejectedTicket() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithStatus(
                        TicketStatus.REJECTED,
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankRejectionReason() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createTicketWithStatus(
                        TicketStatus.REJECTED,
                        "   "
                )
        );
    }

    @Test
    void shouldCreateRejectedTicketWithReason() {
        Ticket ticket = createTicketWithStatus(
                TicketStatus.REJECTED,
                "Ticket illisible"
        );

        assertEquals(TicketStatus.REJECTED, ticket.getStatus());
        assertEquals("Ticket illisible", ticket.getRejectionReason());
    }

    @Test
    void shouldCreateValidatedTicket() {
        Ticket ticket = createTicketWithStatus(
                TicketStatus.VALIDATED,
                null
        );

        assertEquals(TicketStatus.VALIDATED, ticket.getStatus());
        assertTrue(ticket.isValidated());
        assertNull(ticket.getRejectionReason());
    }

    @Test
    void shouldReturnFalseWhenTicketIsNotValidated() {
        Ticket ticket = createTicketWithStatus(
                TicketStatus.PENDING,
                null
        );

        assertFalse(ticket.isValidated());
    }

    @Test
    void shouldReturnTrueWhenTicketIsValidated() {
        Ticket ticket = createTicketWithStatus(
                TicketStatus.VALIDATED,
                null
        );

        assertTrue(ticket.isValidated());
    }

    @Test
    void shouldValidatePendingTicket() {
        Ticket ticket = createTicket();

        ticket.validate();

        assertEquals(TicketStatus.VALIDATED, ticket.getStatus());
        assertNull(ticket.getRejectionReason());
        assertTrue(ticket.isValidated());
    }

    @Test
    void shouldRejectValidationWhenTicketIsNotPending() {
        Ticket ticket = createTicketWithStatus(
                TicketStatus.REJECTED,
                "Ticket illisible"
        );

        assertThrows(
                IllegalStateException.class,
                ticket::validate
        );

        assertEquals(TicketStatus.REJECTED, ticket.getStatus());
        assertEquals("Ticket illisible", ticket.getRejectionReason());
    }

    @Test
    void shouldRejectPendingTicket() {
        Ticket ticket = createTicket();

        ticket.reject("Ticket illisible");

        assertEquals(TicketStatus.REJECTED, ticket.getStatus());
        assertEquals("Ticket illisible", ticket.getRejectionReason());
    }

    @Test
    void shouldRejectRejectionWhenTicketIsNotPending() {
        Ticket ticket = createTicketWithStatus(
                TicketStatus.VALIDATED,
                null
        );

        assertThrows(
                IllegalStateException.class,
                () -> ticket.reject("Ticket illisible")
        );

        assertEquals(TicketStatus.VALIDATED, ticket.getStatus());
        assertNull(ticket.getRejectionReason());
    }

    @Test
    void shouldMarkPendingTicketAsDuplicate() {
        Ticket ticket = createTicket();

        ticket.markAsDuplicate();

        assertEquals(TicketStatus.DUPLICATE, ticket.getStatus());
        assertNull(ticket.getRejectionReason());
        assertFalse(ticket.isValidated());
    }

    @Test
    void shouldRejectDuplicateWhenTicketIsNotPending() {
        Ticket ticket = createTicketWithStatus(
                TicketStatus.VALIDATED,
                null
        );

        assertThrows(
                IllegalStateException.class,
                ticket::markAsDuplicate
        );

        assertEquals(TicketStatus.VALIDATED, ticket.getStatus());
    }

    @Test
    void shouldGenerateSameFingerprintForSameTicketData() {
        Ticket firstTicket = createTicket();

        Ticket secondTicket = new Ticket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                ticketNumber,
                ticketDate,
                ticketTime,
                amount,
                rawOcrText,
                TicketStatus.PENDING,
                null,
                createdAt
        );

        assertEquals(
                firstTicket.getFingerprintHash(),
                secondTicket.getFingerprintHash()
        );
    }

    @Test
    void shouldGenerateDifferentFingerprintWhenTicketDataChanges() {
        Ticket firstTicket = createTicket();

        Ticket secondTicket = new Ticket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-456",
                ticketDate,
                ticketTime,
                amount,
                rawOcrText,
                TicketStatus.PENDING,
                null,
                createdAt
        );

        assertNotEquals(
                firstTicket.getFingerprintHash(),
                secondTicket.getFingerprintHash()
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Ticket createTicket() {
        return createTicket(amount);
    }

    private Ticket createTicket(BigDecimal amount) {
        return new Ticket(
                id,
                merchantId,
                customerId,
                ticketNumber,
                ticketDate,
                ticketTime,
                amount,
                rawOcrText,
                TicketStatus.PENDING,
                null,
                createdAt
        );
    }

    private Ticket createTicketWithTicketNumber(String number) {
        return new Ticket(
                id,
                merchantId,
                customerId,
                number,
                ticketDate,
                ticketTime,
                amount,
                rawOcrText,
                TicketStatus.PENDING,
                null,
                createdAt
        );
    }

    private Ticket createTicketWithTicketDate(LocalDate date) {
        return new Ticket(
                id,
                merchantId,
                customerId,
                ticketNumber,
                date,
                ticketTime,
                amount,
                rawOcrText,
                TicketStatus.PENDING,
                null,
                createdAt
        );
    }

    private Ticket createTicketWithTicketTime(LocalTime time) {
        return new Ticket(
                id,
                merchantId,
                customerId,
                ticketNumber,
                ticketDate,
                time,
                amount,
                rawOcrText,
                TicketStatus.PENDING,
                null,
                createdAt
        );
    }

    private Ticket createTicketWithStatus(
            TicketStatus status,
            String rejectionReason
    ) {
        return new Ticket(
                id,
                merchantId,
                customerId,
                ticketNumber,
                ticketDate,
                ticketTime,
                amount,
                rawOcrText,
                status,
                rejectionReason,
                createdAt
        );
    }

    private Ticket createTicketWithCreatedAt(LocalDateTime dateTime) {
        return new Ticket(
                id,
                merchantId,
                customerId,
                ticketNumber,
                ticketDate,
                ticketTime,
                amount,
                rawOcrText,
                TicketStatus.PENDING,
                null,
                dateTime
        );
    }
}