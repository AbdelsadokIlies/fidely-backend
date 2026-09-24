package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.users.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux profils clients
 * persistés en base de données.
 *
 * <p>Ce repository est utilisé uniquement par la couche infrastructure.
 * La couche applicative utilise le port de sortie correspondant.</p>
 */
public interface SpringDataCustomerRepository
        extends JpaRepository<CustomerEntity, UUID> {

    /**
     * Recherche le profil d'un client à partir de l'identifiant
     * de son utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return profil client s'il existe
     */
    Optional<CustomerEntity> findByUserId(UUID userId);
}