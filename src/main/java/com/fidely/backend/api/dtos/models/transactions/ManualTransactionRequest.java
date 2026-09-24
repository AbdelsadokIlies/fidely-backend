package com.fidely.backend.api.dtos.models.transactions;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Requête utilisée pour enregistrer manuellement une transaction
 * de fidélité par un marchand.
 *
 * <p>Contrairement à une transaction issue d'un ticket, aucune
 * donnée de ticket n'est nécessaire. Le montant permet de calculer
 * automatiquement le nombre de points à attribuer selon la règle
 * de fidélité du marchand.</p>
 *
 * @param loyaltyId identifiant du programme de fidélité
 * @param merchantId identifiant du marchand associé à la transaction
 * @param customerId identifiant du client associé à la transaction
 * @param amount montant de la transaction
 */
public record ManualTransactionRequest(

        @NotNull(message = "Loyalty ID is required")
        UUID loyaltyId,

        @NotNull(message = "Merchant ID is required")
        UUID merchantId,

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount
) { }