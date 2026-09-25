package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.auth.RefreshSession;

import java.util.UUID;

/**
 * Port d'entrée définissant les opérations applicatives
 * liées aux sessions de refresh token.
 *
 * <p>Ce port expose les opérations nécessaires à la création,
 * à la rotation et à la révocation des refresh tokens.</p>
 *
 * <p>La gestion HTTP des cookies et la génération des JWT
 * restent en dehors de ce port.</p>
 */
public interface IRefreshSessionService {

    /**
     * Crée une nouvelle session de refresh token pour un utilisateur.
     *
     * <p>La méthode retourne à la fois la session persistée et
     * le refresh token en clair destiné au client. Le token en clair
     * ne doit jamais être persisté.</p>
     *
     * @param userId identifiant de l'utilisateur
     * @return résultat de création de la session de refresh
     */
    RefreshTokenCreationResult createSession(UUID userId);

    /**
     * Effectue la rotation d'un refresh token existant.
     *
     * <p>Le token présenté est invalidé et un nouveau token est
     * généré. En cas de réutilisation d'un token déjà révoqué,
     * la famille entière de sessions doit être révoquée.</p>
     *
     * @param refreshToken refresh token présenté par le client
     * @return résultat de rotation contenant le nouveau token
     */
    RefreshTokenRotationResult rotate(String refreshToken);

    /**
     * Révoque toutes les sessions appartenant à une famille.
     *
     * @param familyId identifiant de la famille de sessions
     */
    void revokeFamily(UUID familyId);

    /**>
     * Révoque la session correspondant à un refresh token.
     *
     * <p>La famille entière de sessions est révoquée afin
     * d'invalider également les éventuels tokens issus
     * de rotations précédentes.</p>
     *
     * @param refreshToken refresh token présenté par le client
     */
    void revokeSession(String refreshToken);

    /**
     * Résultat de la création d'une session de refresh.
     *
     * @param session session persistée
     * @param refreshToken token en clair destiné au client
     */
    record RefreshTokenCreationResult(
            RefreshSession session,
            String refreshToken
    ) {
    }

    /**
     * Résultat d'une rotation de refresh token.
     *
     * @param previousSession session correspondant à l'ancien token
     * @param newSession nouvelle session créée
     * @param refreshToken nouveau token en clair destiné au client
     */
    record RefreshTokenRotationResult(
            RefreshSession previousSession,
            RefreshSession newSession,
            String refreshToken
    ) {
    }
}