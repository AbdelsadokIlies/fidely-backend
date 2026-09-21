package com.fidely.backend.infrastructure.ocr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record OcrTicketData(
        String ticketNumber,
        LocalDate ticketDate,
        LocalTime ticketTime,
        BigDecimal amount,
        String rawText
) {
}