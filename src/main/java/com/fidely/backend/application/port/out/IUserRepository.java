package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.users.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant l'accès aux comptes utilisateurs.
 *
 * <p>Ce port abstrait l'accès à la persistance des utilisateurs
 * pour la couche applicative. Il ne dépend d'aucune technologie
 * de persistance.</p>
 */
public interface IUserRepository {

    /**
     * Recherche un utilisateur à partir de son identifiant.
     *
     * @param userId identifiant de l'utilisateur
     * @return l'utilisateur s'il existe
     */
    Optional<User> findById(UUID userId);

    /**
     * Recherche un utilisateur à partir de son adresse e-mail.
     *
     * @param email adresse e-mail de l'utilisateur
     * @return l'utilisateur s'il existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Enregistre un utilisateur.
     *
     * @param user utilisateur à enregistrer
     * @return utilisateur enregistré
     */
    User save(User user);

    /**
     * Supprime un utilisateur à partir de son identifiant.
     *
     * @param userId identifiant de l'utilisateur
     */
    void deleteById(UUID userId);
}