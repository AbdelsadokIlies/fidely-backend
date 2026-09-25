package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.IntegrationTest;
import com.fidely.backend.domain.models.auth.AuthIdentity;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataAuthIdentityRepository;
import com.fidely.backend.infrastructure.entities.models.auth.AuthIdentityEntity;
import com.fidely.backend.infrastructure.entities.mappers.auth.AuthIdentityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link AuthIdentityRepositoryAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class AuthIdentityRepositoryAdapterTest {

    @Mock
    private SpringDataAuthIdentityRepository identityRepository;

    @Mock
    private AuthIdentityMapper identityMapper;

    private AuthIdentityRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuthIdentityRepositoryAdapter(
                identityRepository,
                identityMapper
        );
    }

    @Test
    void shouldFindIdentityById() {
        UUID id = UUID.randomUUID();
        AuthIdentityEntity entity = createEntity(id);
        AuthIdentity domain = createDomain(id);

        when(identityRepository.findById(id))
                .thenReturn(Optional.of(entity));
        when(identityMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<AuthIdentity> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(identityRepository).findById(id);
        verify(identityMapper).toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenIdentityDoesNotExistById() {
        UUID id = UUID.randomUUID();

        when(identityRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<AuthIdentity> result = adapter.findById(id);

        assertTrue(result.isEmpty());

        verify(identityRepository).findById(id);
    }

    @Test
    void shouldFindIdentityByUserIdAndProvider() {
        UUID userId = UUID.randomUUID();
        AuthIdentityEntity entity = createEntity(UUID.randomUUID());
        AuthIdentity domain = createDomain(entity.getId());

        when(identityRepository.findByUserIdAndProvider(
                userId,
                "email"
        )).thenReturn(Optional.of(entity));

        when(identityMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<AuthIdentity> result =
                adapter.findByUserIdAndProvider(userId, "email");

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(identityRepository)
                .findByUserIdAndProvider(userId, "email");
        verify(identityMapper).toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenIdentityDoesNotExistByUserIdAndProvider() {
        UUID userId = UUID.randomUUID();

        when(identityRepository.findByUserIdAndProvider(
                userId,
                "email"
        )).thenReturn(Optional.empty());

        Optional<AuthIdentity> result =
                adapter.findByUserIdAndProvider(userId, "email");

        assertTrue(result.isEmpty());

        verify(identityRepository)
                .findByUserIdAndProvider(userId, "email");
    }

    @Test
    void shouldFindIdentityByProviderAndProviderUserId() {
        AuthIdentityEntity entity = createEntity(UUID.randomUUID());
        AuthIdentity domain = createDomain(entity.getId());

        when(identityRepository.findByProviderAndProviderUserId(
                "google",
                "google-user-123"
        )).thenReturn(Optional.of(entity));

        when(identityMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<AuthIdentity> result =
                adapter.findByProviderAndProviderUserId(
                        "google",
                        "google-user-123"
                );

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(identityRepository)
                .findByProviderAndProviderUserId(
                        "google",
                        "google-user-123"
                );
        verify(identityMapper).toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenIdentityDoesNotExistByProviderAndProviderUserId() {
        when(identityRepository.findByProviderAndProviderUserId(
                "google",
                "unknown-user"
        )).thenReturn(Optional.empty());

        Optional<AuthIdentity> result =
                adapter.findByProviderAndProviderUserId(
                        "google",
                        "unknown-user"
                );

        assertTrue(result.isEmpty());

        verify(identityRepository)
                .findByProviderAndProviderUserId(
                        "google",
                        "unknown-user"
                );
    }

    @Test
    void shouldSaveIdentity() {
        UUID id = UUID.randomUUID();
        AuthIdentity domain = createDomain(id);
        AuthIdentityEntity entity = createEntity(id);
        AuthIdentityEntity savedEntity = createEntity(id);
        AuthIdentity savedDomain = createDomain(id);

        when(identityMapper.toEntity(domain))
                .thenReturn(entity);
        when(identityRepository.save(entity))
                .thenReturn(savedEntity);
        when(identityMapper.toDomain(savedEntity))
                .thenReturn(savedDomain);

        AuthIdentity result = adapter.save(domain);

        assertSame(savedDomain, result);

        verify(identityMapper).toEntity(domain);
        verify(identityRepository).save(entity);
        verify(identityMapper).toDomain(savedEntity);
    }

    @Test
    void shouldDeleteIdentityById() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(identityRepository).deleteById(id);
    }

    private AuthIdentityEntity createEntity(UUID id) {
        return new AuthIdentityEntity(
                id,
                UUID.randomUUID(),
                "email",
                null,
                "$2a$10$hashed-password",
                LocalDateTime.now()
        );
    }

    private AuthIdentity createDomain(UUID id) {
        return new AuthIdentity(
                id,
                UUID.randomUUID(),
                "email",
                null,
                "$2a$10$hashed-password",
                LocalDateTime.now()
        );
    }
}