package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.users.MerchantManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux profils des
 * gestionnaires de marchands persistés en base de données.
 *
 * <p>Ce repository est utilisé uniquement par la couche infrastructure.
 * La couche applicative utilise le port de sortie correspondant.</p>
 */
public interface SpringDataMerchantManagerRepository
        extends JpaRepository<MerchantManagerEntity, UUID> {

    /**
     * Recherche le profil d'un gestionnaire à partir de l'identifiant
     * de son utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return profil gestionnaire s'il existe
     */
    Optional<MerchantManagerEntity> findByUserId(UUID userId);
}