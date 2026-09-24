package com.fidely.backend.api.dtos.models.transactions;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Requête utilisée pour créer une transaction de fidélité
 * à partir des données d'un ticket préalablement extrait par OCR.
 *
 * <p>Le ticket n'étant pas encore persisté après l'appel OCR,
 * les données nécessaires à sa création effective sont renvoyées
 * par le client lors de cette seconde étape.</p>
 *
 * <p>L'identifiant de la fidélité permet de déterminer le programme
 * de fidélité auquel les points doivent être attribués.</p>
 *
 * @param loyaltyId identifiant du programme de fidélité
 * @param merchantId identifiant du marchand associé au ticket
 * @param customerId identifiant du client associé au ticket
 * @param ticketNumber numéro du ticket
 * @param ticketDate date du ticket
 * @param ticketTime heure du ticket
 * @param amount montant du ticket
 * @param rawOcrText texte brut extrait par OCR
 */
public record CreateTransactionRequest(

        @NotNull(message = "Loyalty ID is required")
        UUID loyaltyId,

        @NotNull(message = "Merchant ID is required")
        UUID merchantId,

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotBlank(message = "Ticket number is required")
        String ticketNumber,

        @NotNull(message = "Ticket date is required")
        LocalDate ticketDate,

        @NotNull(message = "Ticket time is required")
        LocalTime ticketTime,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        String rawOcrText
) { }