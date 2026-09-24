package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.users.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux comptes utilisateurs
 * persistés en base de données.
 *
 * <p>Cette interface constitue l'abstraction Spring Data utilisée
 * par l'adapter de persistance. Elle ne doit pas être utilisée
 * directement par la couche applicative.</p>
 */
public interface SpringDataUserRepository
        extends JpaRepository<UserEntity, UUID> {

    /**
     * Recherche un utilisateur à partir de son adresse e-mail.
     *
     * @param email adresse e-mail recherchée
     * @return l'utilisateur correspondant s'il existe
     */
    Optional<UserEntity> findByEmail(String email);
}