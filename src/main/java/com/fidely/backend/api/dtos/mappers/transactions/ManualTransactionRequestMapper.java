package com.fidely.backend.api.dtos.mappers.transactions;

import com.fidely.backend.api.dtos.models.transactions.ManualTransactionRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de transformer une requête de transaction
 * manuelle en données utilisables par la couche applicative.
 *
 * <p>Ce mapper ne contient aucune logique métier et ne réalise
 * aucun accès à la persistance.</p>
 */
@Component
public class ManualTransactionRequestMapper {

    /**
     * Vérifie que la requête est valide et la retourne pour son
     * utilisation par la couche applicative.
     *
     * @param request requête de transaction manuelle
     * @return requête de transaction manuelle
     * @throws IllegalArgumentException si la requête est null
     */
    public ManualTransactionRequest toRequest(
            ManualTransactionRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Manual transaction request cannot be null"
            );
        }

        return request;
    }
}