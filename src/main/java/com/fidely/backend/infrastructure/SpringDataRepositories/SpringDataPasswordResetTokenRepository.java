package com.fidely.backend.infrastructure.SpringDataRepositories;

import com.fidely.backend.infrastructure.entities.models.auth.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data permettant l'accès aux tokens
 * de réinitialisation de mot de passe.
 */
public interface SpringDataPasswordResetTokenRepository
        extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE PasswordResetTokenEntity p
    SET p.usedAt = :usedAt
    WHERE p.userId = :userId
      AND p.usedAt IS NULL
    """)
    void invalidateByUserId(
            @Param("userId") UUID userId,
            @Param("usedAt") LocalDateTime usedAt
    );

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE PasswordResetTokenEntity p
    SET p.usedAt = :usedAt
    WHERE p.id = :id
      AND p.usedAt IS NULL
    """)
    void markAsUsed(
            @Param("id") UUID id,
            @Param("usedAt") LocalDateTime usedAt
    );
}