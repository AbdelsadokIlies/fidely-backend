package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.wheels.WheelParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant d'accéder aux participations
 * aux roues de récompenses.
 */
public interface SpringDataWheelParticipationRepository
        extends JpaRepository<WheelParticipationEntity, UUID> {

    /**
     * Récupère les participations d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des participations
     */
    List<WheelParticipationEntity> findByCustomerId(UUID customerId);

    /**
     * Récupère la dernière participation d'un client à une roue.
     *
     * @param customerId identifiant du client
     * @param wheelId identifiant de la roue
     * @return la dernière participation si elle existe
     */
    Optional<WheelParticipationEntity> findTopByCustomerIdAndWheelIdOrderByPlayedAtDesc(
            UUID customerId,
            UUID wheelId
    );
}