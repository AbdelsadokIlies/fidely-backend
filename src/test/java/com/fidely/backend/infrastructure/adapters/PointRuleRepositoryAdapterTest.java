package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataPointRuleRepository;
import com.fidely.backend.infrastructure.entities.models.loyalties.Rewards.PointRuleEntity;
import com.fidely.backend.infrastructure.entities.mappers.loyalties.Rewards.PointRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointRuleRepositoryAdapterTest {

    @Mock
    private SpringDataPointRuleRepository pointRuleRepository;

    @Mock
    private PointRuleMapper pointRuleMapper;

    private PointRuleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PointRuleRepositoryAdapter(
                pointRuleRepository,
                pointRuleMapper
        );
    }

    @Test
    void shouldFindPointRuleById() {
        UUID id = UUID.randomUUID();

        PointRuleEntity entity = createEntity(id);
        PointRule domain = createDomain(id);

        when(pointRuleRepository.findById(id))
                .thenReturn(Optional.of(entity));

        when(pointRuleMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<PointRule> result =
                adapter.findById(id);

        assertThat(result)
                .isPresent()
                .contains(domain);

        verify(pointRuleRepository)
                .findById(id);

        verify(pointRuleMapper)
                .toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenPointRuleDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(pointRuleRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<PointRule> result =
                adapter.findById(id);

        assertThat(result).isEmpty();

        verify(pointRuleRepository)
                .findById(id);

        verifyNoInteractions(pointRuleMapper);
    }

    @Test
    void shouldFindPointRulesByMerchantId() {
        UUID merchantId = UUID.randomUUID();

        PointRuleEntity entity1 =
                createEntity(UUID.randomUUID());

        PointRuleEntity entity2 =
                createEntity(UUID.randomUUID());

        PointRule domain1 =
                createDomain(entity1.getId());

        PointRule domain2 =
                createDomain(entity2.getId());

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(entity1, entity2));

        when(pointRuleMapper.toDomain(entity1))
                .thenReturn(domain1);

        when(pointRuleMapper.toDomain(entity2))
                .thenReturn(domain2);

        List<PointRule> result =
                adapter.findByMerchantId(merchantId);

        assertThat(result)
                .containsExactly(domain1, domain2);

        verify(pointRuleRepository)
                .findByMerchantId(merchantId);

        verify(pointRuleMapper)
                .toDomain(entity1);

        verify(pointRuleMapper)
                .toDomain(entity2);
    }

    @Test
    void shouldReturnEmptyWhenMerchantHasNoPointRules() {
        UUID merchantId = UUID.randomUUID();

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        List<PointRule> result =
                adapter.findByMerchantId(merchantId);

        assertThat(result).isEmpty();

        verify(pointRuleRepository)
                .findByMerchantId(merchantId);

        verifyNoInteractions(pointRuleMapper);
    }

    @Test
    void shouldSavePointRule() {
        UUID merchantId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        PointRule domain = createDomain(pointRuleId);
        PointRuleEntity entity = createEntity(pointRuleId);
        PointRuleEntity savedEntity = createEntity(pointRuleId);
        PointRule savedDomain = createDomain(pointRuleId);

        when(pointRuleMapper.toEntity(domain, merchantId))
                .thenReturn(entity);

        when(pointRuleRepository.save(entity))
                .thenReturn(savedEntity);

        when(pointRuleMapper.toDomain(savedEntity))
                .thenReturn(savedDomain);

        PointRule result =
                adapter.save(domain, merchantId);

        assertThat(result)
                .isSameAs(savedDomain);

        verify(pointRuleMapper)
                .toEntity(domain, merchantId);

        verify(pointRuleRepository)
                .save(entity);

        verify(pointRuleMapper)
                .toDomain(savedEntity);
    }

    @Test
    void shouldDeletePointRuleById() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(pointRuleRepository)
                .deleteById(id);
    }

    private PointRuleEntity createEntity(UUID id) {
        return new PointRuleEntity(
                id,
                UUID.randomUUID(),
                new BigDecimal("1.5000"),
                (short) 1,
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    private PointRule createDomain(UUID id) {
        return new PointRule(
                id,
                new BigDecimal("1.5000"),
                RoundingMethod.ROUND,
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }
}