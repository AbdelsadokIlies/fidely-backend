package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.wheels.WheelPrizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository Spring Data permettant d'accéder aux lots des roues.
 */
public interface SpringDataWheelPrizeRepository extends JpaRepository<WheelPrizeEntity, UUID> {

    /**
     * Récupère tous les lots associés à une roue.
     *
     * @param wheelId identifiant de la roue
     * @return liste des lots
     */
    List<WheelPrizeEntity> findByWheelId(UUID wheelId);
}