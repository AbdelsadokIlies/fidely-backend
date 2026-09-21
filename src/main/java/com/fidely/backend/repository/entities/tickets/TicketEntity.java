package com.fidely.backend.repository.entities.tickets;

import com.fidely.backend.domain.models.tickets.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entité JPA représentant un ticket en base de données.
 *
 * <p>Cette classe appartient à la couche de persistance et ne doit pas
 * contenir de logique métier.</p>
 */
@Entity
@Table(name = "tickets")
public class TicketEntity {

    /**
     * Identifiant du ticket.
     */
    @Id
    private UUID id;

    /**
     * Identifiant du marchand associé au ticket.
     */
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /**
     * Identifiant du client associé au ticket.
     */
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /**
     * Numéro du ticket.
     */
    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    /**
     * Empreinte unique permettant notamment de détecter les doublons.
     */
    @Column(name = "fingerprint_hash", nullable = false)
    private String fingerprintHash;

    /**
     * Date du ticket.
     */
    @Column(name = "ticket_date", nullable = false)
    private LocalDate ticketDate;

    /**
     * Heure du ticket.
     */
    @Column(name = "ticket_time", nullable = false)
    private LocalTime ticketTime;

    /**
     * Montant du ticket.
     */
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Texte brut extrait par OCR.
     */
    @Column(name = "raw_ocr_text", columnDefinition = "TEXT")
    private String rawOcrText;

    /**
     * Référence vers l'image stockée du ticket.
     */
    @Column(name = "image_storage_ref")
    private String imageStorageRef;

    /**
     * Statut actuel du ticket.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    /**
     * Raison du rejet du ticket.
     */
    @Column(name = "rejection_reason")
    private String rejectionReason;

    /**
     * Date de création de l'enregistrement.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructeur requis par JPA.
     */
    protected TicketEntity() {
    }

    /**
     * Crée une entité représentant un ticket.
     *
     * @param id identifiant du ticket
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @param ticketNumber numéro du ticket
     * @param fingerprintHash empreinte du ticket
     * @param ticketDate date du ticket
     * @param ticketTime heure du ticket
     * @param amount montant du ticket
     * @param rawOcrText texte extrait par OCR
     * @param imageStorageRef référence de l'image
     * @param status statut du ticket
     * @param rejectionReason raison du rejet
     * @param createdAt date de création
     */
    public TicketEntity(
            UUID id,
            UUID merchantId,
            UUID customerId,
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
        this.id = id;
        this.merchantId = merchantId;
        this.customerId = customerId;
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

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}