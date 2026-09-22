package com.fidely.backend.infrastructure.ocr;

import com.fidely.backend.application.port.out.IOcrService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implémentation fictive du service OCR utilisée pour les tests
 * et le développement sans véritable moteur de reconnaissance optique.
 */
@Service
public class FakeOcrService implements IOcrService {

    @Override
    public OcrTicketData extractTicketData(byte[] image) {

        String ticketNumber = generateTicketNumber();
        BigDecimal amount = generateAmount();
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        String rawText = """
                RESTAURANT FIDELY
                Ticket: %s
                Date: %s
                Heure: %s
                TOTAL: %s EUR
                """.formatted(
                ticketNumber,
                date,
                time,
                amount
        );

        return new OcrTicketData(
                ticketNumber,
                date,
                time,
                amount,
                rawText
        );
    }

    private String generateTicketNumber() {
        return "TICKET-" + UUID.randomUUID();
    }

    private BigDecimal generateAmount() {
        double amount = ThreadLocalRandom.current()
                .nextDouble(5.00, 150.00);

        return BigDecimal
                .valueOf(amount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}