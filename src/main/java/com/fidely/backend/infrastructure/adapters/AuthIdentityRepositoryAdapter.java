package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IAuthIdentityRepository;
import com.fidely.backend.domain.models.auth.AuthIdentity;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataAuthIdentityRepository;
import com.fidely.backend.infrastructure.entities.models.auth.AuthIdentityEntity;
import com.fidely.backend.infrastructure.entities.mappers.auth.AuthIdentityMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur de repository permettant à la couche application
 * d'accéder à la persistance des identités d'authentification.
 *
 * <p>Cet adaptateur fait le lien entre le port de sortie
 * {@link IAuthIdentityRepository} et le repository Spring Data JPA.</p>
 *
 * <p>La conversion entre le modèle du domaine et l'entité JPA
 * est déléguée à {@link AuthIdentityMapper}.</p>
 */
@Repository
public class AuthIdentityRepositoryAdapter
        implements IAuthIdentityRepository {

    private final SpringDataAuthIdentityRepository identityRepository;
    private final AuthIdentityMapper identityMapper;

    /**
     * Construit l'adaptateur du repository des identités.
     *
     * @param identityRepository repository Spring Data JPA
     * @param identityMapper mapper entre domaine et persistence
     */
    public AuthIdentityRepositoryAdapter(
            SpringDataAuthIdentityRepository identityRepository,
            AuthIdentityMapper identityMapper
    ) {
        this.identityRepository = identityRepository;
        this.identityMapper = identityMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AuthIdentity> findById(UUID id) {
        return identityRepository.findById(id)
                .map(identityMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AuthIdentity> findByUserIdAndProvider(
            UUID userId,
            String provider
    ) {
        return identityRepository
                .findByUserIdAndProvider(userId, provider)
                .map(identityMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AuthIdentity> findByProviderAndProviderUserId(
            String provider,
            String providerUserId
    ) {
        return identityRepository
                .findByProviderAndProviderUserId(
                        provider,
                        providerUserId
                )
                .map(identityMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthIdentity save(AuthIdentity identity) {
        AuthIdentityEntity entity = identityMapper.toEntity(identity);
        AuthIdentityEntity savedEntity =
                identityRepository.save(entity);

        return identityMapper.toDomain(savedEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(UUID id) {
        identityRepository.deleteById(id);
    }
}