package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.auth.AuthIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant la persistance des identités
 * d'authentification.
 *
 * <p>Cette interface constitue la couche d'accès aux données JPA
 * pour les identités d'authentification. Elle est utilisée par
 * l'adaptateur de repository et n'est pas exposée directement
 * à la couche application.</p>
 */
public interface SpringDataAuthIdentityRepository
        extends JpaRepository<AuthIdentityEntity, UUID> {

    /**
     * Recherche une identité associée à un utilisateur et à un fournisseur.
     *
     * @param userId identifiant de l'utilisateur
     * @param provider fournisseur d'authentification
     * @return l'identité correspondante si elle existe
     */
    Optional<AuthIdentityEntity> findByUserIdAndProvider(
            UUID userId,
            String provider
    );

    /**
     * Recherche une identité à partir du fournisseur et de l'identifiant
     * fourni par celui-ci.
     *
     * @param provider fournisseur d'authentification
     * @param providerUserId identifiant externe de l'utilisateur
     * @return l'identité correspondante si elle existe
     */
    Optional<AuthIdentityEntity> findByProviderAndProviderUserId(
            String provider,
            String providerUserId
    );
}