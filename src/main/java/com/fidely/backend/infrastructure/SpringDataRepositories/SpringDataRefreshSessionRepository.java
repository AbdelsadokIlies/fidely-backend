package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.auth.RefreshSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux sessions
 * de rafraîchissement.
 */
public interface SpringDataRefreshSessionRepository
        extends JpaRepository<RefreshSessionEntity, UUID> {

    Optional<RefreshSessionEntity> findByTokenHash(String tokenHash);

    List<RefreshSessionEntity> findByFamilyId(UUID familyId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE RefreshSessionEntity r
            SET r.revokedAt = :revokedAt
            WHERE r.familyId = :familyId
              AND r.revokedAt IS NULL
            """)
    void revokeFamily(
            @Param("familyId") UUID familyId,
            @Param("revokedAt") LocalDateTime revokedAt
    );
}