package com.fidely.backend.infrastructure.ocr;

import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OcrTicketMapper {

    public Ticket toDomain(
            OcrTicketData data,
            UUID merchantId,
            UUID customerId,
            String fingerprintHash
    ) {
        return new Ticket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                data.ticketNumber(),
                fingerprintHash,
                data.ticketDate(),
                data.ticketTime(),
                data.amount(),
                data.rawText(),
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }
}