package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.auth.AuthIdentity;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant l'accès aux identités
 * d'authentification.
 *
 * <p>Ce port abstrait la persistance des identités utilisées
 * pour authentifier les utilisateurs. Il ne dépend d'aucune
 * technologie de persistance.</p>
 */
public interface IAuthIdentityRepository {

    /**
     * Recherche une identité à partir de son identifiant.
     *
     * @param id identifiant de l'identité
     * @return l'identité si elle existe
     */
    Optional<AuthIdentity> findById(UUID id);

    /**
     * Recherche une identité à partir de son utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @param provider fournisseur d'authentification
     * @return l'identité correspondante si elle existe
     */
    Optional<AuthIdentity> findByUserIdAndProvider(
            UUID userId,
            String provider
    );

    /**
     * Recherche une identité à partir de son fournisseur
     * et de son identifiant externe.
     *
     * <p>Cette opération sera notamment nécessaire pour les futurs
     * fournisseurs comme Google ou Apple.</p>
     *
     * @param provider fournisseur d'authentification
     * @param providerUserId identifiant fourni par le fournisseur
     * @return l'identité correspondante si elle existe
     */
    Optional<AuthIdentity> findByProviderAndProviderUserId(
            String provider,
            String providerUserId
    );

    /**
     * Enregistre une identité d'authentification.
     *
     * @param identity identité à enregistrer
     * @return identité enregistrée
     */
    AuthIdentity save(AuthIdentity identity);

    /**
     * Supprime une identité à partir de son identifiant.
     *
     * @param id identifiant de l'identité
     */
    void deleteById(UUID id);
}