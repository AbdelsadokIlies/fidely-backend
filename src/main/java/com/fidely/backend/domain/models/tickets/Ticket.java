package com.fidely.backend.domain.models.tickets;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Représente un ticket de caisse associé à un client et à un marchand.
 *
 * <p>Un ticket peut être en attente de validation, validé, rejeté ou identifié
 * comme doublon. Lorsqu'un ticket est rejeté, une raison doit être conservée.</p>
 *
 * <p>L'empreinte du ticket est calculée automatiquement à partir du marchand,
 * du numéro du ticket, de sa date, de son heure et de son montant. Elle permet
 * d'identifier les tickets déjà traités.</p>
 */
public class Ticket {

    private final UUID id;
    private final UUID merchantId;
    private final UUID customerId;

    private final String ticketNumber;
    private final String fingerprintHash;

    private final LocalDate ticketDate;
    private final LocalTime ticketTime;
    private final BigDecimal amount;

    private final String rawOcrText;

    private TicketStatus status;
    private String rejectionReason;

    private final LocalDateTime createdAt;

    /**
     * Crée un nouveau ticket.
     *
     * <p>L'empreinte du ticket est générée automatiquement à partir des données
     * métier permettant d'identifier un ticket de manière déterministe.</p>
     *
     * @param id identifiant du ticket
     * @param merchantId id du marchand associé au ticket
     * @param customerId id du client associé au ticket
     * @param ticketNumber numéro du ticket
     * @param ticketDate date du ticket
     * @param ticketTime heure du ticket
     * @param amount montant du ticket
     * @param rawOcrText texte brut extrait par OCR
     * @param status statut initial du ticket
     * @param rejectionReason raison du rejet, obligatoire si le ticket est rejeté
     * @param createdAt date de création du ticket
     * @throws IllegalArgumentException si une donnée obligatoire est invalide
     * ou si la raison de rejet ne correspond pas au statut du ticket
     */
    public Ticket(
            UUID id,
            UUID merchantId,
            UUID customerId,
            String ticketNumber,
            LocalDate ticketDate,
            LocalTime ticketTime,
            BigDecimal amount,
            String rawOcrText,
            TicketStatus status,
            String rejectionReason,
            LocalDateTime createdAt
    ) {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant cannot be null");
        }

        if (customerId == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("Ticket number cannot be null or blank");
        }

        if (ticketDate == null) {
            throw new IllegalArgumentException("Ticket date cannot be null");
        }

        if (ticketTime == null) {
            throw new IllegalArgumentException("Ticket time cannot be null");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Created at cannot be null");
        }

        if (status != TicketStatus.REJECTED && rejectionReason != null) {
            throw new IllegalArgumentException(
                    "Rejection reason is only allowed for rejected tickets"
            );
        }

        if (status == TicketStatus.REJECTED &&
                (rejectionReason == null || rejectionReason.isBlank())) {
            throw new IllegalArgumentException(
                    "Rejected ticket must have a rejection reason"
            );
        }

        this.id = id;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.ticketNumber = ticketNumber;
        this.ticketDate = ticketDate;
        this.ticketTime = ticketTime;
        this.amount = amount;
        this.fingerprintHash = generateFingerprintHash();
        this.rawOcrText = rawOcrText;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
    }

    /**
     * Génère l'empreinte unique du ticket.
     *
     * <p>L'empreinte est calculée à partir des informations suivantes :
     * merchantId, ticketNumber, ticketDate, ticketTime et amount.</p>
     *
     * @return empreinte SHA-256 du ticket sous forme hexadécimale
     * @throws IllegalStateException si l'algorithme SHA-256 n'est pas disponible
     */
    private String generateFingerprintHash() {
        String data = String.join(
                "|",
                merchantId.toString(),
                ticketNumber,
                ticketDate.toString(),
                ticketTime.toString(),
                amount.toPlainString()
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(data.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    /**
     * Valide le ticket.
     *
     * <p>Seul un ticket en attente peut être validé. Une éventuelle raison
     * de rejet est supprimée lors de la validation.</p>
     *
     * @throws IllegalStateException si le ticket n'est pas en attente
     */
    public void validate() {
        if (status != TicketStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending tickets can be validated"
            );
        }

        status = TicketStatus.VALIDATED;
        rejectionReason = null;
    }

    /**
     * Rejette le ticket avec une raison.
     *
     * <p>Seul un ticket en attente peut être rejeté.</p>
     *
     * @param reason raison du rejet
     * @throws IllegalStateException si le ticket n'est pas en attente
     * @throws IllegalArgumentException si la raison est null ou vide
     */
    public void reject(String reason) {
        if (status != TicketStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending tickets can be rejected"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Rejection reason cannot be null or blank"
            );
        }

        status = TicketStatus.REJECTED;
        rejectionReason = reason;
    }

    /**
     * Marque le ticket comme doublon.
     *
     * <p>Seul un ticket en attente peut être marqué comme doublon.</p>
     *
     * @throws IllegalStateException si le ticket n'est pas en attente
     */
    public void markAsDuplicate() {
        if (status != TicketStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending tickets can be marked as duplicate"
            );
        }

        status = TicketStatus.DUPLICATE;
        rejectionReason = null;
    }

    /**
     * Vérifie si le ticket a été validé.
     *
     * @return true si le ticket est validé, sinon false
     */
    public boolean isValidated() {
        return status == TicketStatus.VALIDATED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getFingerprintHash() {
        return fingerprintHash;
    }

    public LocalDate getTicketDate() {
        return ticketDate;
    }

    public LocalTime getTicketTime() {
        return ticketTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getRawOcrText() {
        return rawOcrText;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}