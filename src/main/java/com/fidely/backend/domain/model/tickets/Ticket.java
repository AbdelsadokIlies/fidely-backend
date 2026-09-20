package com.fidely.backend.domain.model.tickets;

import com.fidely.backend.domain.model.Merchants.Merchant;
import com.fidely.backend.domain.model.users.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**

 * Représente un ticket de caisse associé à un client et à un marchand.
 *
 * <p>Un ticket peut être en attente de validation, validé, rejeté ou identifié
 * comme doublon. Lorsqu'un ticket est rejeté, une raison doit être conservée.</p>
 */
public class Ticket {

    private final UUID id;
    private final Merchant merchant;
    private final Customer customer;

    private final String ticketNumber;
    private final String fingerprintHash;

    private final LocalDate ticketDate;
    private final LocalTime ticketTime;
    private final BigDecimal amount;

    private final String rawOcrText;
    private final String imageStorageRef;

    private TicketStatus status;
    private String rejectionReason;

    private final LocalDateTime createdAt;

    /**

     * Crée un nouveau ticket.
     *
     * @param id identifiant du ticket
     * @param merchant marchand associé au ticket
     * @param customer client associé au ticket
     * @param ticketNumber numéro du ticket
     * @param fingerprintHash empreinte permettant d'identifier les doublons
     * @param ticketDate date du ticket
     * @param ticketTime heure du ticket
     * @param amount montant du ticket
     * @param rawOcrText texte brut extrait par OCR
     * @param imageStorageRef référence de l'image du ticket stockée
     * @param status statut initial du ticket
     * @param rejectionReason raison du rejet, obligatoire si le ticket est rejeté
     * @param createdAt date de création du ticket
     * @throws IllegalArgumentException si une donnée obligatoire est invalide
     * ou si la raison de rejet ne correspond pas au statut du ticket
     */
    public Ticket(
            UUID id,
            Merchant merchant,
            Customer customer,
            String ticketNumber,
            String fingerprintHash,
            LocalDate ticketDate,
            LocalTime ticketTime,
            BigDecimal amount,
            String rawOcrText,
            String imageStorageRef,
            TicketStatus status,
            String rejectionReason,
            LocalDateTime createdAt
    ) {
        if (merchant == null) {
            throw new IllegalArgumentException("Merchant cannot be null");
        }

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("Ticket number cannot be null or blank");
        }

        if (fingerprintHash == null || fingerprintHash.isBlank()) {
            throw new IllegalArgumentException("Fingerprint hash cannot be null or blank");
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

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
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
        this.merchant = merchant;
        this.customer = customer;
        this.ticketNumber = ticketNumber;
        this.fingerprintHash = fingerprintHash;
        this.ticketDate = ticketDate;
        this.ticketTime = ticketTime;
        this.amount = amount;
        this.rawOcrText = rawOcrText;
        this.imageStorageRef = imageStorageRef;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
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

    public Merchant getMerchant() {
        return merchant;
    }

    public Customer getCustomer() {
        return customer;
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

    public String getImageStorageRef() {
        return imageStorageRef;
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
