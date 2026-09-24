package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IRefreshSessionRepository;
import com.fidely.backend.domain.models.auth.RefreshSession;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataRefreshSessionRepository;
import com.fidely.backend.infrastructure.entities.models.auth.RefreshSessionEntity;
import com.fidely.backend.infrastructure.entities.mappers.auth.RefreshSessionMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur permettant à la couche application
 * d'accéder à la persistance des sessions de refresh.
 *
 * <p>Cet adaptateur fait le lien entre le port de sortie
 * {@link IRefreshSessionRepository} et Spring Data JPA.</p>
 */
@Repository
public class RefreshSessionRepositoryAdapter
        implements IRefreshSessionRepository {

    private final SpringDataRefreshSessionRepository refreshSessionRepository;
    private final RefreshSessionMapper refreshSessionMapper;

    /**
     * Construit l'adaptateur du repository des sessions de refresh.
     *
     * @param refreshSessionRepository repository Spring Data JPA
     * @param refreshSessionMapper mapper entre domaine et persistence
     */
    public RefreshSessionRepositoryAdapter(
            SpringDataRefreshSessionRepository refreshSessionRepository,
            RefreshSessionMapper refreshSessionMapper
    ) {
        this.refreshSessionRepository = refreshSessionRepository;
        this.refreshSessionMapper = refreshSessionMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RefreshSession> findById(UUID id) {
        return refreshSessionRepository.findById(id)
                .map(refreshSessionMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RefreshSession> findByTokenHash(
            String tokenHash
    ) {
        return refreshSessionRepository
                .findByTokenHash(tokenHash)
                .map(refreshSessionMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RefreshSession> findByFamilyId(
            UUID familyId
    ) {
        return refreshSessionRepository
                .findByFamilyId(familyId)
                .stream()
                .map(refreshSessionMapper::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefreshSession save(RefreshSession session) {
        RefreshSessionEntity entity =
                refreshSessionMapper.toEntity(session);

        RefreshSessionEntity savedEntity =
                refreshSessionRepository.save(entity);

        return refreshSessionMapper.toDomain(savedEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revokeFamily(
            UUID familyId,
            LocalDateTime revokedAt
    ) {
        refreshSessionRepository.revokeFamily(
                familyId,
                revokedAt
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(UUID id) {
        refreshSessionRepository.deleteById(id);
    }
}