package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IPasswordResetTokenRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataPasswordResetTokenRepository;
import com.fidely.backend.infrastructure.entities.mappers.auth.PasswordResetTokenMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur permettant au domaine d'accéder au stockage
 * des tokens de réinitialisation de mot de passe.
 */
@Repository
public class PasswordResetTokenRepositoryAdapter
        implements IPasswordResetTokenRepository {

    private final SpringDataPasswordResetTokenRepository repository;
    private final PasswordResetTokenMapper mapper;

    public PasswordResetTokenRepositoryAdapter(
            SpringDataPasswordResetTokenRepository repository,
            PasswordResetTokenMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PasswordResetTokenData> findByTokenHash(
            String tokenHash
    ) {
        return repository.findByTokenHash(tokenHash)
                .map(mapper::toData);
    }

    @Override
    public void invalidateByUserId(
            UUID userId,
            LocalDateTime usedAt
    ) {
        repository.invalidateByUserId(userId, usedAt);
    }

    @Override
    public void markAsUsed(
            UUID id,
            LocalDateTime usedAt
    ) {
        repository.markAsUsed(id, usedAt);
    }

    @Override
    public PasswordResetTokenData save(
            PasswordResetTokenData token
    ) {
        return mapper.toData(
                repository.save(mapper.toEntity(token))
        );
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}