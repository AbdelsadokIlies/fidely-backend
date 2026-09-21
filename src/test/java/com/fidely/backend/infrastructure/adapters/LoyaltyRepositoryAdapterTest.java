package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyTransactionRepository;
import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyEntity;
import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyTransactionEntity;
import com.fidely.backend.infrastructure.mappers.loyalties.LoyaltyMapper;
import com.fidely.backend.infrastructure.mappers.loyalties.LoyaltyTransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyRepositoryAdapterTest {

    @Mock
    private SpringDataLoyaltyRepository loyaltyRepository;

    @Mock
    private LoyaltyMapper loyaltyMapper;

    @Mock
    private SpringDataLoyaltyTransactionRepository transactionRepository;

    @Mock
    private LoyaltyTransactionMapper transactionMapper;

    private LoyaltyRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LoyaltyRepositoryAdapter(
                loyaltyRepository,
                transactionRepository,
                loyaltyMapper,
                transactionMapper
        );
    }

    // ---------------------------------------------------------
    // Loyalty
    // ---------------------------------------------------------

    @Test
    void shouldFindLoyaltyById() {
        UUID id = UUID.randomUUID();

        LoyaltyEntity entity = createEntity(id);
        Loyalty domain = createDomain(id);

        when(loyaltyRepository.findById(id))
                .thenReturn(Optional.of(entity));

        when(loyaltyMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<Loyalty> result =
                adapter.findById(id);

        assertThat(result)
                .isPresent()
                .contains(domain);

        verify(loyaltyRepository)
                .findById(id);

        verify(loyaltyMapper)
                .toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenLoyaltyDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(loyaltyRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<Loyalty> result =
                adapter.findById(id);

        assertThat(result).isEmpty();

        verify(loyaltyRepository)
                .findById(id);

        verifyNoInteractions(loyaltyMapper);
    }

    @Test
    void shouldFindLoyaltyByCustomerAndMerchant() {
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        LoyaltyEntity entity = createEntity(UUID.randomUUID());
        Loyalty domain = createDomain(entity.getId());

        when(
                loyaltyRepository.findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                )
        ).thenReturn(Optional.of(entity));

        when(loyaltyMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<Loyalty> result =
                adapter.findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                );

        assertThat(result)
                .isPresent()
                .contains(domain);

        verify(loyaltyRepository)
                .findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                );

        verify(loyaltyMapper)
                .toDomain(entity);
    }

    @Test
    void shouldFindLoyaltiesByCustomerId() {
        UUID customerId = UUID.randomUUID();

        LoyaltyEntity entity1 = createEntity(UUID.randomUUID());
        LoyaltyEntity entity2 = createEntity(UUID.randomUUID());

        Loyalty loyalty1 = createDomain(entity1.getId());
        Loyalty loyalty2 = createDomain(entity2.getId());

        when(loyaltyRepository.findByCustomerId(customerId))
                .thenReturn(List.of(entity1, entity2));

        when(loyaltyMapper.toDomain(entity1))
                .thenReturn(loyalty1);

        when(loyaltyMapper.toDomain(entity2))
                .thenReturn(loyalty2);

        List<Loyalty> result =
                adapter.findByCustomerId(customerId);

        assertThat(result)
                .containsExactly(loyalty1, loyalty2);

        verify(loyaltyRepository)
                .findByCustomerId(customerId);

        verify(loyaltyMapper)
                .toDomain(entity1);

        verify(loyaltyMapper)
                .toDomain(entity2);
    }

    @Test
    void shouldFindLoyaltiesByMerchantId() {
        UUID merchantId = UUID.randomUUID();

        LoyaltyEntity entity1 = createEntity(UUID.randomUUID());
        LoyaltyEntity entity2 = createEntity(UUID.randomUUID());

        Loyalty loyalty1 = createDomain(entity1.getId());
        Loyalty loyalty2 = createDomain(entity2.getId());

        when(loyaltyRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(entity1, entity2));

        when(loyaltyMapper.toDomain(entity1))
                .thenReturn(loyalty1);

        when(loyaltyMapper.toDomain(entity2))
                .thenReturn(loyalty2);

        List<Loyalty> result =
                adapter.findByMerchantId(merchantId);

        assertThat(result)
                .containsExactly(loyalty1, loyalty2);

        verify(loyaltyRepository)
                .findByMerchantId(merchantId);

        verify(loyaltyMapper)
                .toDomain(entity1);

        verify(loyaltyMapper)
                .toDomain(entity2);
    }

    @Test
    void shouldSaveLoyalty() {
        UUID id = UUID.randomUUID();

        Loyalty domain = createDomain(id);
        LoyaltyEntity entity = createEntity(id);
        LoyaltyEntity savedEntity = createEntity(id);

        Loyalty savedDomain = createDomain(id);

        when(loyaltyMapper.toEntity(domain))
                .thenReturn(entity);

        when(loyaltyRepository.save(entity))
                .thenReturn(savedEntity);

        when(loyaltyMapper.toDomain(savedEntity))
                .thenReturn(savedDomain);

        Loyalty result =
                adapter.save(domain);

        assertThat(result)
                .isSameAs(savedDomain);

        verify(loyaltyMapper)
                .toEntity(domain);

        verify(loyaltyRepository)
                .save(entity);

        verify(loyaltyMapper)
                .toDomain(savedEntity);
    }

    @Test
    void shouldDeleteLoyaltyById() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(loyaltyRepository)
                .deleteById(id);
    }

    // ---------------------------------------------------------
    // Loyalty transactions
    // ---------------------------------------------------------

    @Test
    void shouldSaveTransaction() {
        UUID transactionId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        LoyaltyTransaction transaction =
                createTransaction(
                        transactionId,
                        loyaltyId,
                        ticketId
                );

        LoyaltyTransactionEntity entity =
                createTransactionEntity(
                        transactionId,
                        loyaltyId,
                        ticketId
                );

        LoyaltyTransactionEntity savedEntity =
                createTransactionEntity(
                        transactionId,
                        loyaltyId,
                        ticketId
                );

        LoyaltyTransaction savedTransaction =
                createTransaction(
                        transactionId,
                        loyaltyId,
                        ticketId
                );

        when(transactionMapper.toEntity(transaction))
                .thenReturn(entity);

        when(transactionRepository.save(entity))
                .thenReturn(savedEntity);

        when(transactionMapper.toDomain(savedEntity))
                .thenReturn(savedTransaction);

        LoyaltyTransaction result =
                adapter.saveTransaction(transaction);

        assertThat(result)
                .isSameAs(savedTransaction);

        verify(transactionMapper)
                .toEntity(transaction);

        verify(transactionRepository)
                .save(entity);

        verify(transactionMapper)
                .toDomain(savedEntity);
    }

    @Test
    void shouldFindTransactionsByLoyaltyId() {
        UUID loyaltyId = UUID.randomUUID();

        LoyaltyTransactionEntity entity1 =
                createTransactionEntity(
                        UUID.randomUUID(),
                        loyaltyId,
                        UUID.randomUUID()
                );

        LoyaltyTransactionEntity entity2 =
                createTransactionEntity(
                        UUID.randomUUID(),
                        loyaltyId,
                        UUID.randomUUID()
                );

        LoyaltyTransaction transaction1 =
                createTransaction(
                        entity1.getId(),
                        loyaltyId,
                        entity1.getTicketId()
                );

        LoyaltyTransaction transaction2 =
                createTransaction(
                        entity2.getId(),
                        loyaltyId,
                        entity2.getTicketId()
                );

        when(transactionRepository.findByLoyaltyId(loyaltyId))
                .thenReturn(List.of(entity1, entity2));

        when(transactionMapper.toDomain(entity1))
                .thenReturn(transaction1);

        when(transactionMapper.toDomain(entity2))
                .thenReturn(transaction2);

        List<LoyaltyTransaction> result =
                adapter.findTransactionsByLoyaltyId(loyaltyId);

        assertThat(result)
                .containsExactly(transaction1, transaction2);

        verify(transactionRepository)
                .findByLoyaltyId(loyaltyId);

        verify(transactionMapper)
                .toDomain(entity1);

        verify(transactionMapper)
                .toDomain(entity2);
    }

    @Test
    void shouldReturnEmptyWhenNoTransactionsForLoyalty() {
        UUID loyaltyId = UUID.randomUUID();

        when(transactionRepository.findByLoyaltyId(loyaltyId))
                .thenReturn(List.of());

        List<LoyaltyTransaction> result =
                adapter.findTransactionsByLoyaltyId(loyaltyId);

        assertThat(result).isEmpty();

        verify(transactionRepository)
                .findByLoyaltyId(loyaltyId);

        verifyNoInteractions(transactionMapper);
    }

    @Test
    void shouldFindTransactionsByTicketId() {
        UUID ticketId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();

        LoyaltyTransactionEntity entity =
                createTransactionEntity(
                        UUID.randomUUID(),
                        loyaltyId,
                        ticketId
                );

        LoyaltyTransaction transaction =
                createTransaction(
                        entity.getId(),
                        loyaltyId,
                        ticketId
                );

        when(transactionRepository.findByTicketId(ticketId))
                .thenReturn(List.of(entity));

        when(transactionMapper.toDomain(entity))
                .thenReturn(transaction);

        List<LoyaltyTransaction> result =
                adapter.findTransactionsByTicketId(ticketId);

        assertThat(result)
                .containsExactly(transaction);

        verify(transactionRepository)
                .findByTicketId(ticketId);

        verify(transactionMapper)
                .toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenNoTransactionsForTicket() {
        UUID ticketId = UUID.randomUUID();

        when(transactionRepository.findByTicketId(ticketId))
                .thenReturn(List.of());

        List<LoyaltyTransaction> result =
                adapter.findTransactionsByTicketId(ticketId);

        assertThat(result).isEmpty();

        verify(transactionRepository)
                .findByTicketId(ticketId);

        verifyNoInteractions(transactionMapper);
    }

    // ---------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------

    private LoyaltyEntity createEntity(UUID id) {
        LocalDateTime now = LocalDateTime.now();

        return new LoyaltyEntity(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                100,
                now,
                now
        );
    }

    private Loyalty createDomain(UUID id) {
        LocalDateTime now = LocalDateTime.now();

        return new Loyalty(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                100,
                now,
                now
        );
    }

    private LoyaltyTransactionEntity createTransactionEntity(
            UUID id,
            UUID loyaltyId,
            UUID ticketId
    ) {
        return new LoyaltyTransactionEntity(
                id,
                loyaltyId,
                ticketId,
                100,
                "Test transaction",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    private LoyaltyTransaction createTransaction(
            UUID id,
            UUID loyaltyId,
            UUID ticketId
    ) {
        return new LoyaltyTransaction(
                id,
                loyaltyId,
                ticketId,
                100,
                "Test transaction",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }
}