package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.auth.RefreshSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant de gérer la persistance
 * des sessions de refresh token.
 *
 * <p>Cette interface appartient à la couche application
 * et ne dépend d'aucune technologie de persistance.</p>
 */
public interface IRefreshSessionRepository {

    /**
     * Recherche une session par son identifiant.
     *
     * @param id identifiant de la session
     * @return session correspondante si elle existe
     */
    Optional<RefreshSession> findById(UUID id);

    /**
     * Recherche une session à partir du hash du refresh token.
     *
     * @param tokenHash hash du refresh token
     * @return session correspondante si elle existe
     */
    Optional<RefreshSession> findByTokenHash(String tokenHash);

    /**
     * Recherche toutes les sessions appartenant à une famille
     * de rotation donnée.
     *
     * @param familyId identifiant de la famille
     * @return sessions de la famille
     */
    List<RefreshSession> findByFamilyId(UUID familyId);

    /**
     * Sauvegarde une session de refresh token.
     *
     * @param session session à sauvegarder
     * @return session sauvegardée
     */
    RefreshSession save(RefreshSession session);

    /**
     * Révoque toutes les sessions appartenant à une famille
     * de rotation donnée.
     *
     * @param familyId identifiant de la famille
     * @param revokedAt date de révocation
     */
    void revokeFamily(UUID familyId, LocalDateTime revokedAt);

    /**
     * Supprime une session à partir de son identifiant.
     *
     * @param id identifiant de la session
     */
    void deleteById(UUID id);
}